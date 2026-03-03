package ru.lifegame.assets.infrastructure.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.assets.domain.model.asset.AnimationSpec;
import ru.lifegame.assets.domain.model.asset.AssetLayer;
import ru.lifegame.assets.domain.model.asset.AssetSpec;
import ru.lifegame.assets.domain.model.asset.ColorPalette;
import ru.lifegame.assets.domain.service.AssetGenerationService;
import ru.lifegame.assets.infrastructure.writer.AtlasConfigWriter;
import ru.lifegame.assets.infrastructure.writer.PngLayerWriter;
import ru.lifegame.assets.infrastructure.writer.WebpAtlasWriter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ProceduralTextureGenerator v2 — generates layered PNG assets and
 * horizontal-strip animation atlases from XML-driven AssetSpec.
 * <p>
 * Reads specifications parsed from docs/prompts/{entity-type}/{entity-name}/visual-specs.xml.
 * Generates 32-bit RGBA PNGs for each layer and animation atlases with atlas-config.json metadata.
 */
public class LayeredAssetGenerator implements AssetGenerationService {

    private static final Logger log = LoggerFactory.getLogger(LayeredAssetGenerator.class);

    private static final int DEFAULT_LAYER_WIDTH = 128;
    private static final int DEFAULT_LAYER_HEIGHT = 128;

    private final PngLayerWriter pngWriter;
    private final WebpAtlasWriter atlasWriter;
    private final AtlasConfigWriter configWriter;

    public LayeredAssetGenerator(PngLayerWriter pngWriter,
                                 WebpAtlasWriter atlasWriter,
                                 AtlasConfigWriter configWriter) {
        this.pngWriter = pngWriter;
        this.atlasWriter = atlasWriter;
        this.configWriter = configWriter;
    }

    @Override
    public List<Path> generateAsset(AssetSpec spec, Path outputRoot) {
        List<Path> generated = new ArrayList<>();
        Path entityDir = outputRoot.resolve(spec.naming().outputDir());

        // Generate static layers
        for (AssetLayer layer : spec.layers()) {
            BufferedImage layerImage = generateLayerImage(layer, spec.colorPalette(), spec);
            try {
                Path written = pngWriter.write(layerImage, layer, entityDir);
                generated.add(written);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to write layer: " + layer.id(), e);
            }
        }

        // Generate animation atlases
        if (!spec.animations().isEmpty()) {
            Path animDir = entityDir.resolve("animations");
            for (AnimationSpec animSpec : spec.animations()) {
                List<BufferedImage> frames = generateAnimationFrames(animSpec, spec.colorPalette());
                try {
                    Path atlasPath = atlasWriter.writeAtlas(frames, animSpec, animDir);
                    generated.add(atlasPath);

                    String atlasFileName = atlasPath.getFileName().toString();
                    Path configPath = configWriter.writeConfig(animSpec, atlasFileName, animDir);
                    generated.add(configPath);
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed to write atlas: " + animSpec.name(), e);
                }
            }

            // Write combined atlas-config.json
            try {
                Path combinedConfig = configWriter.writeCombinedConfig(spec.animations(), animDir);
                generated.add(combinedConfig);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to write combined atlas config", e);
            }
        }

        log.info("Generated {} files for {}/{}", generated.size(),
                spec.entityType(), spec.entityName());
        return generated;
    }

    /**
     * Generates a single layer image. Each layer gets a procedural fill
     * based on its type and the asset's color palette.
     */
    BufferedImage generateLayerImage(AssetLayer layer, ColorPalette palette, AssetSpec spec) {
        int width = DEFAULT_LAYER_WIDTH;
        int height = DEFAULT_LAYER_HEIGHT;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // Transparent background (RGBA with alpha=0)
        g.setBackground(new Color(0, 0, 0, 0));
        g.clearRect(0, 0, width, height);

        Color fillColor = resolveLayerColor(layer, palette);
        long seed = (spec.entityName() + layer.id()).hashCode();
        Random rng = new Random(seed);

        // Procedural fill: scattered rectangles within a region
        int regionX = width / 8;
        int regionY = height / 8;
        int regionW = width * 3 / 4;
        int regionH = height * 3 / 4;

        adjustRegionForLayerType(layer.type());

        int numShapes = 10 + rng.nextInt(20);
        for (int i = 0; i < numShapes; i++) {
            int alpha = 120 + rng.nextInt(136);
            Color shapeColor = new Color(
                    fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), alpha);
            g.setColor(shapeColor);

            int x = regionX + rng.nextInt(regionW);
            int y = regionY + rng.nextInt(regionH);
            int w = 4 + rng.nextInt(width / 4);
            int h = 4 + rng.nextInt(height / 4);
            g.fillRect(x, y, w, h);
        }

        g.dispose();
        return image;
    }

    /**
     * Generates animation frames with subtle per-frame variation.
     */
    List<BufferedImage> generateAnimationFrames(AnimationSpec animSpec, ColorPalette palette) {
        List<BufferedImage> frames = new ArrayList<>();
        int fw = animSpec.frameWidth();
        int fh = animSpec.frameHeight();

        Color baseColor = paletteColor(palette, 0);
        long seed = animSpec.name().hashCode();
        Random rng = new Random(seed);

        for (int i = 0; i < animSpec.frames(); i++) {
            BufferedImage frame = new BufferedImage(fw, fh, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frame.createGraphics();
            g.setBackground(new Color(0, 0, 0, 0));
            g.clearRect(0, 0, fw, fh);

            // Draw a simple animated shape (circle moving slightly)
            int offsetX = (int) (Math.sin(2.0 * Math.PI * i / animSpec.frames()) * fw / 8);
            int offsetY = (int) (Math.cos(2.0 * Math.PI * i / animSpec.frames()) * fh / 16);

            int cx = fw / 2 + offsetX;
            int cy = fh / 2 + offsetY;
            int radius = fw / 4;

            g.setColor(new Color(baseColor.getRed(), baseColor.getGreen(),
                    baseColor.getBlue(), 200));
            g.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

            // Add some noise
            int numDots = 5 + rng.nextInt(10);
            for (int d = 0; d < numDots; d++) {
                int dx = rng.nextInt(fw);
                int dy = rng.nextInt(fh);
                g.setColor(new Color(baseColor.getRed(), baseColor.getGreen(),
                        baseColor.getBlue(), 60 + rng.nextInt(80)));
                g.fillRect(dx, dy, 2, 2);
            }

            g.dispose();
            frames.add(frame);
        }
        return frames;
    }

    private Color resolveLayerColor(AssetLayer layer, ColorPalette palette) {
        return switch (layer.type().toLowerCase()) {
            case "background" -> paletteColor(palette, 0);
            case "midground" -> paletteColor(palette, 1);
            case "foreground" -> paletteColor(palette, 2);
            case "base" -> paletteColor(palette, 0);
            case "outfit" -> paletteColor(palette, 3);
            case "overlay" -> paletteColor(palette, 4);
            default -> paletteColor(palette, 0);
        };
    }

    private Color paletteColor(ColorPalette palette, int index) {
        List<String> colors = palette.primary();
        if (colors.isEmpty()) {
            return new Color(0xF5E6D3);
        }
        String hex = colors.get(index % colors.size());
        return hexToColor(hex);
    }

    private Color hexToColor(String hex) {
        String cleaned = hex.startsWith("#") ? hex.substring(1) : hex;
        int r = Integer.parseInt(cleaned.substring(0, 2), 16);
        int g = Integer.parseInt(cleaned.substring(2, 4), 16);
        int b = Integer.parseInt(cleaned.substring(4, 6), 16);
        return new Color(r, g, b);
    }

    private void adjustRegionForLayerType(String type) {
        // Hook for future per-type region adjustments
    }
}
