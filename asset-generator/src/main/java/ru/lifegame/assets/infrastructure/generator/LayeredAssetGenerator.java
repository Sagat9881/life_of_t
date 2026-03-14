package ru.lifegame.assets.infrastructure.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.assets.domain.model.asset.*;
import ru.lifegame.assets.domain.service.AssetGenerationService;
import ru.lifegame.assets.infrastructure.writer.AtlasConfigWriter;
import ru.lifegame.assets.infrastructure.writer.PngLayerWriter;
import ru.lifegame.assets.infrastructure.writer.WebpAtlasWriter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Generates layered PNG assets and animation atlases from XML-driven AssetSpec.
 * All animation metadata is written to a single <b>sprite-atlas.json</b> per entity
 * (config version 2.0) via {@link AtlasConfigWriter}.
 *
 * Output structure per entity:
 * <pre>
 *   outputRoot/{type}/{name}/
 *     {name}.png                       <- composite static image
 *     {layer-id}.png                   <- individual layer PNGs
 *     sprite-atlas.json                <- atlas config (consumed by frontend + AnimationContentService)
 *     animations/
 *       {anim}_atlas.png               <- grid animation atlases (one per animation name)
 * </pre>
 *
 * Multi-row grids: each AnimationVariant becomes one row (row_0, row_1, ...).
 * A variant-less animation gets a single row_0 with condition={ "all": [] }.
 * Overlay animations via layer conditions are pending variant-based refactor.
 */
public class LayeredAssetGenerator implements AssetGenerationService {

    private static final Logger log = LoggerFactory.getLogger(LayeredAssetGenerator.class);
    private static final int DEFAULT_WIDTH = 128;
    private static final int DEFAULT_HEIGHT = 128;
    private static final double DEFAULT_CHARACTER_DISPLAY_SCALE = 3.0;
    private static final double DEFAULT_LOCATION_DISPLAY_SCALE = 1.0;

    private final UniversalPixelRenderer renderer;
    private final PngLayerWriter pngWriter;
    private final WebpAtlasWriter atlasWriter;
    private final AtlasConfigWriter configWriter;

    public LayeredAssetGenerator(UniversalPixelRenderer renderer,
                                 PngLayerWriter pngWriter,
                                 WebpAtlasWriter atlasWriter,
                                 AtlasConfigWriter configWriter) {
        this.renderer = renderer;
        this.pngWriter = pngWriter;
        this.atlasWriter = atlasWriter;
        this.configWriter = configWriter;
    }

    @Override
    public List<Path> generateAsset(AssetSpec spec, Path outputRoot) {
        List<Path> generated = new ArrayList<>();
        Path entityDir = outputRoot.resolve(spec.naming().outputDir());

        int width = resolveWidth(spec);
        int height = resolveHeight(spec);

        // 1. Composite static image
        BufferedImage composite = renderer.renderComposite(spec.layers(), width, height);
        try {
            Path compositePath = pngWriter.writeToPath(composite,
                    entityDir.resolve(spec.entityName() + ".png"));
            generated.add(compositePath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write composite: " + spec.entityName(), e);
        }

        // 2. Individual layers
        for (AssetLayer layer : spec.layers()) {
            BufferedImage layerImage = renderer.renderLayer(layer, width, height);
            try {
                Path written = pngWriter.write(layerImage, layer, entityDir);
                generated.add(written);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to write layer: " + layer.id(), e);
            }
        }

        // 3. Animation atlases (all grid) + sprite-atlas.json
        Path animDir = entityDir.resolve("animations");
        boolean hasCharacterAnims = !spec.animations().isEmpty();
        boolean hasOverlayLayers = spec.layers().stream().anyMatch(AssetLayer::hasConditions);

        if (hasCharacterAnims || hasOverlayLayers) {
            generated.addAll(generateAllAtlases(spec, entityDir, animDir, width, height));
        }

        log.info("Generated {} files for {}/{}", generated.size(),
                spec.entityType(), spec.entityName());
        return generated;
    }

    private List<Path> generateAllAtlases(AssetSpec spec, Path entityDir, Path animDir,
                                          int bgWidth, int bgHeight) {
        List<Path> generated = new ArrayList<>();
        List<AssetLayer> layers = spec.layers();

        Map<String, AtlasConfigWriter.GridAnimDef> gridAnimDefs = new LinkedHashMap<>();
        Map<String, AtlasConfigWriter.CropOffsetDef> cropOffsets = new LinkedHashMap<>();

        for (AnimationSpec animSpec : spec.animations()) {
            List<BufferedImage> frames = renderer.renderAnimationFrames(layers, animSpec);

            int[] bounds = renderer.computeCropBounds(frames);
            boolean needsCrop = bounds[0] != 0 || bounds[1] != 0
                    || (!frames.isEmpty()
                        && (bounds[2] != frames.getFirst().getWidth()
                            || bounds[3] != frames.getFirst().getHeight()));

            if (needsCrop) {
                frames = renderer.cropFrames(frames, bounds);
                cropOffsets.put(animSpec.name(), new AtlasConfigWriter.CropOffsetDef(
                        bounds[0], bounds[1],
                        animSpec.frameWidth(), animSpec.frameHeight(),
                        bounds[2], bounds[3]));
                log.info("Cropped grid atlas '{}': {}x{} -> {}x{} (offset {}, {})",
                        animSpec.name(),
                        animSpec.frameWidth(), animSpec.frameHeight(),
                        bounds[2], bounds[3], bounds[0], bounds[1]);
            }

            LinkedHashMap<String, List<BufferedImage>> rowFrames = new LinkedHashMap<>();
            List<AtlasConfigSchema.RowDef> rowDefs = new ArrayList<>();

            if (animSpec.variants().isEmpty()) {
                rowFrames.put("row_0", frames);
                rowDefs.add(new AtlasConfigSchema.RowDef(
                        0,
                        new AtlasConfigSchema.RowCondition(List.of()),
                        animSpec.fps(),
                        animSpec.loop()
                ));
            } else {
                for (int i = 0; i < animSpec.variants().size(); i++) {
                    AnimationVariant variant = animSpec.variants().get(i);
                    rowFrames.put("row_" + i, frames); // same pixels, different playback speed
                    List<AtlasConfigSchema.SingleCondition> predicates = variant.conditions().stream()
                            .map(c -> new AtlasConfigSchema.SingleCondition(
                                    c.space(), c.npcId(), c.stat(), c.operator(), c.value()))
                            .toList();
                    rowDefs.add(new AtlasConfigSchema.RowDef(
                            i,
                            new AtlasConfigSchema.RowCondition(predicates),
                            variant.fps(),
                            variant.loop()
                    ));
                }
            }

            log.info("Grid atlas '{}': {} rows (variants={})",
                    animSpec.name(), rowDefs.size(), animSpec.variants().size());

            try {
                Path atlasPath = atlasWriter.writeGridAtlas(rowFrames, animSpec.name(), animDir);
                generated.add(atlasPath);
                gridAnimDefs.put(animSpec.name(),
                        new AtlasConfigWriter.GridAnimDef(rowDefs, animSpec));
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to write grid atlas: " + animSpec.name(), e);
            }
        }

        // Overlay animations via layer conditions -- pending variant-based refactor
        Map<String, AtlasConfigWriter.OverlayAnimDef> overlayAnimDefs = Map.of();

        double scale = resolveDisplayScale(spec);
        configWriter.withDisplayScale(scale);

        try {
            Path configPath = configWriter.writeSpriteAtlas(
                    spec.entityName(), gridAnimDefs,
                    overlayAnimDefs, cropOffsets, entityDir);
            generated.add(configPath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write sprite-atlas.json for " + spec.entityName(), e);
        }

        return generated;
    }

    private double resolveDisplayScale(AssetSpec spec) {
        return switch (spec.entityType()) {
            case "characters" -> DEFAULT_CHARACTER_DISPLAY_SCALE;
            case "pets" -> 2.0;
            default -> DEFAULT_LOCATION_DISPLAY_SCALE;
        };
    }

    private int resolveWidth(AssetSpec spec) {
        return spec.layers().stream()
                .mapToInt(l -> {
                    PixelData pd = l.pixelData();
                    if (pd == null || pd.isEmpty()) return Math.max(l.width(), 0);
                    int maxX = pd.rects().stream()
                            .mapToInt(r -> r.x() + r.w()).max().orElse(0);
                    int maxXLine = pd.lines().stream()
                            .mapToInt(line -> line.x() + (line.direction() == PixelLine.Direction.HORIZONTAL ? line.length() : 1))
                            .max().orElse(0);
                    return Math.max(maxX, maxXLine);
                })
                .max()
                .orElse(DEFAULT_WIDTH);
    }

    private int resolveHeight(AssetSpec spec) {
        return spec.layers().stream()
                .mapToInt(l -> {
                    PixelData pd = l.pixelData();
                    if (pd == null || pd.isEmpty()) return Math.max(l.height(), 0);
                    int maxY = pd.rects().stream()
                            .mapToInt(r -> r.y() + r.h()).max().orElse(0);
                    int maxYLine = pd.lines().stream()
                            .mapToInt(line -> line.y() + (line.direction() == PixelLine.Direction.VERTICAL ? line.length() : 1))
                            .max().orElse(0);
                    return Math.max(maxY, maxYLine);
                })
                .max()
                .orElse(DEFAULT_HEIGHT);
    }
}
