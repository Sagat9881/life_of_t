package ru.lifegame.assets.infrastructure.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.assets.domain.model.asset.AnimationSpec;
import ru.lifegame.assets.domain.model.asset.AssetLayer;
import ru.lifegame.assets.domain.model.asset.AssetSpec;
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
 * Animations with time-of-day variants (e.g. idle_morning, idle_day, idle_evening, idle_night)
 * are automatically detected and merged into a single <b>grid atlas</b>.
 * Non-variant animations produce a classic horizontal-strip atlas (single row).
 * <p>
 * All animation metadata is written to a single <b>sprite-atlas.json</b> per character
 * (config version 1.1) via {@link AtlasConfigWriter#writeSpriteAtlas}.
 * Frame dimensions come from each AnimationSpec.frameWidth/frameHeight.
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

        // 1. Composite static image
        int width = resolveWidth(spec);
        int height = resolveHeight(spec);
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

        // 3. Animation atlases
        if (!spec.animations().isEmpty()) {
            Path animDir = entityDir.resolve("animations");
            generated.addAll(generateAnimationAtlases(spec, animDir));
        }

        log.info("Generated {} files for {}/{}", generated.size(),
                spec.entityType(), spec.entityName());
        return generated;
    }

    private List<Path> generateAnimationAtlases(AssetSpec spec, Path animDir) {
        List<Path> generated = new ArrayList<>();
        List<AssetLayer> layers = spec.layers();

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
                if (variants.containsKey(tod)) {
                    sorted.put(tod, variants.get(tod));
                }
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
                log.info("Grid atlas '{}': {} rows × {} frames",
                        baseName, sorted.size(), sorted.values().iterator().next().frames());
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

        // Write unified sprite-atlas.json (v1.1 — per-animation frame dimensions)
        try {
            Path configPath = configWriter.writeSpriteAtlas(
                    spec.entityName(), standaloneAnims, gridAnimDefs, animDir);
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
