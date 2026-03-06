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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates layered PNG assets and animation atlases from XML-driven AssetSpec.
 * <p>
 * Works for both characters and locations:
 * <ul>
 *   <li>Character animations with time-of-day variants → grid atlas</li>
 *   <li>Location overlay layers with conditions (ambient_light) → overlay atlas + config</li>
 *   <li>Non-variant animations → horizontal-strip atlas</li>
 * </ul>
 * All animation metadata is written to a single <b>sprite-atlas.json</b> per entity
 * (config version 1.2) via {@link AtlasConfigWriter}.
 */
public class LayeredAssetGenerator implements AssetGenerationService {

    private static final Logger log = LoggerFactory.getLogger(LayeredAssetGenerator.class);
    private static final int DEFAULT_WIDTH = 128;
    private static final int DEFAULT_HEIGHT = 128;

    private static final List<String> TOD_SUFFIXES = List.of("morning", "day", "evening", "night");
    private static final Pattern TOD_PATTERN = Pattern.compile(
            "^(.+?)_(" + String.join("|", TOD_SUFFIXES) + ")$");

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

        // 3. Animation atlases (character animations + location overlay layers)
        Path animDir = entityDir.resolve("animations");
        boolean hasCharacterAnims = !spec.animations().isEmpty();
        boolean hasOverlayLayers = spec.layers().stream().anyMatch(AssetLayer::hasConditions);

        if (hasCharacterAnims || hasOverlayLayers) {
            generated.addAll(generateAllAtlases(spec, animDir, width, height));
        }

        log.info("Generated {} files for {}/{}", generated.size(),
                spec.entityType(), spec.entityName());
        return generated;
    }

    private List<Path> generateAllAtlases(AssetSpec spec, Path animDir, int bgWidth, int bgHeight) {
        List<Path> generated = new ArrayList<>();
        List<AssetLayer> layers = spec.layers();

        // --- Character-style animations (idle, walk, etc.) ---
        LinkedHashMap<String, LinkedHashMap<String, AnimationSpec>> todGroups = new LinkedHashMap<>();
        List<AnimationSpec> standaloneAnims = new ArrayList<>();

        for (AnimationSpec animSpec : spec.animations()) {
            Matcher m = TOD_PATTERN.matcher(animSpec.name());
            if (m.matches()) {
                String baseName = m.group(1);
                String todValue = m.group(2);
                todGroups.computeIfAbsent(baseName, k -> new LinkedHashMap<>())
                         .put(todValue, animSpec);
            } else {
                standaloneAnims.add(animSpec);
            }
        }

        Map<String, AtlasConfigWriter.GridAnimDef> gridAnimDefs = new LinkedHashMap<>();

        for (var entry : todGroups.entrySet()) {
            String baseName = entry.getKey();
            LinkedHashMap<String, AnimationSpec> variants = entry.getValue();

            LinkedHashMap<String, AnimationSpec> sorted = new LinkedHashMap<>();
            for (String tod : TOD_SUFFIXES) {
                if (variants.containsKey(tod)) sorted.put(tod, variants.get(tod));
            }

            LinkedHashMap<String, List<BufferedImage>> rowFrames = new LinkedHashMap<>();
            for (var rowEntry : sorted.entrySet()) {
                List<BufferedImage> frames = renderer.renderAnimationFrames(layers, rowEntry.getValue());
                rowFrames.put(rowEntry.getKey(), frames);
            }

            try {
                Path atlasPath = atlasWriter.writeGridAtlas(rowFrames, baseName, animDir);
                generated.add(atlasPath);
                gridAnimDefs.put(baseName,
                        new AtlasConfigWriter.GridAnimDef("time_of_day", sorted));
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to write grid atlas: " + baseName, e);
            }
        }

        for (AnimationSpec animSpec : standaloneAnims) {
            List<BufferedImage> frames = renderer.renderAnimationFrames(layers, animSpec);
            try {
                Path atlasPath = atlasWriter.writeAtlas(frames, animSpec, animDir);
                generated.add(atlasPath);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to write atlas: " + animSpec.name(), e);
            }
        }

        // --- Overlay layers with conditions (e.g. ambient_light) ---
        Map<String, AtlasConfigWriter.OverlayAnimDef> overlayAnimDefs = new LinkedHashMap<>();

        for (AssetLayer layer : layers) {
            if (!layer.hasConditions()) continue;

            // Render the layer as a single-frame image for each condition
            LinkedHashMap<String, List<BufferedImage>> overlayRows = new LinkedHashMap<>();
            List<AtlasConfigWriter.OverlayRowDef> rowDefs = new ArrayList<>();

            for (String tod : TOD_SUFFIXES) {
                LayerCondition cond = layer.conditions().stream()
                        .filter(c -> c.timeOfDayValue().equals(tod))
                        .findFirst().orElse(null);
                if (cond == null) continue;

                // Render the base layer pixel data as the overlay frame
                BufferedImage frame = renderer.renderLayer(layer, bgWidth, bgHeight);
                overlayRows.put(tod, List.of(frame));
                rowDefs.add(new AtlasConfigWriter.OverlayRowDef(
                        tod, cond.tint(), cond.opacityAsDouble()));
            }

            if (!overlayRows.isEmpty()) {
                try {
                    Path atlasPath = atlasWriter.writeGridAtlas(overlayRows, layer.id(), animDir);
                    generated.add(atlasPath);
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed to write overlay atlas: " + layer.id(), e);
                }

                int defaultRow = Math.min(1, rowDefs.size() - 1); // default to 'day' (index 1)
                overlayAnimDefs.put(layer.id(), new AtlasConfigWriter.OverlayAnimDef(
                        bgWidth, bgHeight, rowDefs, defaultRow));
            }
        }

        // Write unified sprite-atlas.json (v1.2)
        try {
            Path configPath = configWriter.writeSpriteAtlas(
                    spec.entityName(), standaloneAnims, gridAnimDefs, overlayAnimDefs, animDir);
            generated.add(configPath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write sprite-atlas.json for " + spec.entityName(), e);
        }

        return generated;
    }

    private int resolveWidth(AssetSpec spec) {
        return spec.layers().stream()
                .mapToInt(AssetLayer::width)
                .filter(w -> w > 0)
                .max()
                .orElse(DEFAULT_WIDTH);
    }

    private int resolveHeight(AssetSpec spec) {
        return spec.layers().stream()
                .mapToInt(AssetLayer::height)
                .filter(h -> h > 0)
                .max()
                .orElse(DEFAULT_HEIGHT);
    }
}
