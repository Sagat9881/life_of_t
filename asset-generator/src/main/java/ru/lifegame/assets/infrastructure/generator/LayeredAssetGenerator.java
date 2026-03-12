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
 * All animation metadata is written to a single <b>sprite-atlas.json</b> per entity
 * (config version 1.4) via {@link AtlasConfigWriter}.
 *
 * Output structure per entity:
 * <pre>
 *   outputRoot/{type}/{name}/
 *     {name}.png                       ← composite static image
 *     {layer-id}.png                   ← individual layer PNGs
 *     sprite-atlas.json                ← atlas config (consumed by frontend + AnimationContentService)
 *     animations/
 *       {anim}_atlas.png               ← strip / grid animation atlases
 * </pre>
 *
 * URL mapping (Spring Boot static → frontend):
 *   /assets/{type}/{name}/sprite-atlas.json      → classpath:/assets/{type}/{name}/sprite-atlas.json
 *   /assets/{type}/{name}/animations/*_atlas.png → classpath:/assets/{type}/{name}/animations/*.png
 *
 * Frames are cropped to their non-transparent bounding box before atlas packing.
 * The original crop offset is recorded in sprite-atlas.json so the frontend can
 * position the sprite correctly within the scene.
 */
public class LayeredAssetGenerator implements AssetGenerationService {

    private static final Logger log = LoggerFactory.getLogger(LayeredAssetGenerator.class);
    private static final int DEFAULT_WIDTH = 128;
    private static final int DEFAULT_HEIGHT = 128;
    private static final double DEFAULT_CHARACTER_DISPLAY_SCALE = 3.0;
    private static final double DEFAULT_LOCATION_DISPLAY_SCALE = 1.0;

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

        // 3. Animation atlases
        //    Atlas PNGs → entityDir/animations/
        //    sprite-atlas.json → entityDir/  (one level up from animations/)
        Path animDir = entityDir.resolve("animations");
        boolean hasCharacterAnims = !spec.animations().isEmpty();
        boolean hasOverlayLayers = spec.layers().stream().anyMatch(AssetLayer::hasConditions);

        if (hasCharacterAnims || hasOverlayLayers) {
            // entityDir is passed for sprite-atlas.json placement;
            // animDir is passed for atlas PNG placement inside generateAllAtlases.
            generated.addAll(generateAllAtlases(spec, entityDir, animDir, width, height));
        }

        log.info("Generated {} files for {}/{}", generated.size(),
                spec.entityType(), spec.entityName());
        return generated;
    }

    /**
     * Generates all animation atlases (PNG strips/grids) into {@code animDir},
     * and writes sprite-atlas.json into {@code entityDir}.
     *
     * @param spec       entity asset spec
     * @param entityDir  root entity directory — sprite-atlas.json goes here
     * @param animDir    animations sub-directory — atlas PNGs go here
     * @param bgWidth    canvas width
     * @param bgHeight   canvas height
     */
    private List<Path> generateAllAtlases(AssetSpec spec, Path entityDir, Path animDir,
                                          int bgWidth, int bgHeight) {
        List<Path> generated = new ArrayList<>();
        List<AssetLayer> layers = spec.layers();

        // --- Character-style animations ---
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
        Map<String, AtlasConfigWriter.CropOffsetDef> cropOffsets = new LinkedHashMap<>();

        for (var entry : todGroups.entrySet()) {
            String baseName = entry.getKey();
            LinkedHashMap<String, AnimationSpec> variants = entry.getValue();

            LinkedHashMap<String, AnimationSpec> sorted = new LinkedHashMap<>();
            for (String tod : TOD_SUFFIXES) {
                if (variants.containsKey(tod)) sorted.put(tod, variants.get(tod));
            }

            // Render all rows, collect all frames for crop bounds computation
            LinkedHashMap<String, List<BufferedImage>> rowFrames = new LinkedHashMap<>();
            List<BufferedImage> allFramesForCrop = new ArrayList<>();
            for (var rowEntry : sorted.entrySet()) {
                List<BufferedImage> frames = renderer.renderAnimationFrames(layers, rowEntry.getValue());
                rowFrames.put(rowEntry.getKey(), frames);
                allFramesForCrop.addAll(frames);
            }

            // Compute crop bounds across ALL rows and crop
            int[] bounds = renderer.computeCropBounds(allFramesForCrop);
            boolean needsCrop = bounds[0] != 0 || bounds[1] != 0
                    || (!allFramesForCrop.isEmpty()
                        && (bounds[2] != allFramesForCrop.getFirst().getWidth()
                            || bounds[3] != allFramesForCrop.getFirst().getHeight()));

            if (needsCrop) {
                LinkedHashMap<String, List<BufferedImage>> croppedRows = new LinkedHashMap<>();
                for (var rowEntry : rowFrames.entrySet()) {
                    croppedRows.put(rowEntry.getKey(), renderer.cropFrames(rowEntry.getValue(), bounds));
                }
                rowFrames = croppedRows;

                AnimationSpec firstSpec = sorted.values().iterator().next();
                cropOffsets.put(baseName, new AtlasConfigWriter.CropOffsetDef(
                        bounds[0], bounds[1],
                        firstSpec.frameWidth(), firstSpec.frameHeight(),
                        bounds[2], bounds[3]));
                log.info("Cropped grid atlas '{}': {}x{} -> {}x{} (offset {}, {})",
                        baseName,
                        firstSpec.frameWidth(), firstSpec.frameHeight(),
                        bounds[2], bounds[3], bounds[0], bounds[1]);
            }

            try {
                // Atlas PNGs → animDir
                Path atlasPath = atlasWriter.writeGridAtlas(rowFrames, baseName, animDir);
                generated.add(atlasPath);
                gridAnimDefs.put(baseName,
                        new AtlasConfigWriter.GridAnimDef("time_of_day", sorted));
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to write grid atlas: " + baseName, e);
            }
        }

        // Standalone strip animations
        Map<String, AtlasConfigWriter.CropOffsetDef> stripCropOffsets = new LinkedHashMap<>();

        for (AnimationSpec animSpec : standaloneAnims) {
            List<BufferedImage> frames = renderer.renderAnimationFrames(layers, animSpec);

            int[] bounds = renderer.computeCropBounds(frames);
            boolean needsCrop = bounds[0] != 0 || bounds[1] != 0
                    || (!frames.isEmpty()
                        && (bounds[2] != frames.getFirst().getWidth()
                            || bounds[3] != frames.getFirst().getHeight()));

            if (needsCrop) {
                stripCropOffsets.put(animSpec.name(), new AtlasConfigWriter.CropOffsetDef(
                        bounds[0], bounds[1],
                        animSpec.frameWidth(), animSpec.frameHeight(),
                        bounds[2], bounds[3]));
                frames = renderer.cropFrames(frames, bounds);
                log.info("Cropped strip atlas '{}': {}x{} -> {}x{} (offset {}, {})",
                        animSpec.name(),
                        animSpec.frameWidth(), animSpec.frameHeight(),
                        bounds[2], bounds[3], bounds[0], bounds[1]);
            }

            try {
                // Atlas PNGs → animDir
                Path atlasPath = atlasWriter.writeAtlas(frames, animSpec, animDir);
                generated.add(atlasPath);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to write atlas: " + animSpec.name(), e);
            }
        }

        // --- Overlay layers with conditions ---
        Map<String, AtlasConfigWriter.OverlayAnimDef> overlayAnimDefs = new LinkedHashMap<>();

        for (AssetLayer layer : layers) {
            if (!layer.hasConditions()) continue;

            LinkedHashMap<String, List<BufferedImage>> overlayRows = new LinkedHashMap<>();
            List<AtlasConfigWriter.OverlayRowDef> rowDefs = new ArrayList<>();

            for (String tod : TOD_SUFFIXES) {
                LayerCondition cond = layer.conditions().stream()
                        .filter(c -> c.timeOfDayValue().equals(tod))
                        .findFirst().orElse(null);
                if (cond == null) continue;

                BufferedImage frame = renderer.renderLayer(layer, bgWidth, bgHeight);
                overlayRows.put(tod, List.of(frame));
                rowDefs.add(new AtlasConfigWriter.OverlayRowDef(
                        tod, cond.tint(), cond.opacityAsDouble()));
            }

            if (!overlayRows.isEmpty()) {
                try {
                    // Overlay atlas PNGs → animDir
                    Path atlasPath = atlasWriter.writeGridAtlas(overlayRows, layer.id(), animDir);
                    generated.add(atlasPath);
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed to write overlay atlas: " + layer.id(), e);
                }

                int defaultRow = Math.min(1, rowDefs.size() - 1);
                overlayAnimDefs.put(layer.id(), new AtlasConfigWriter.OverlayAnimDef(
                        bgWidth, bgHeight, rowDefs, defaultRow));
            }
        }

        // Merge all crop offsets
        Map<String, AtlasConfigWriter.CropOffsetDef> allCropOffsets = new LinkedHashMap<>();
        allCropOffsets.putAll(cropOffsets);
        allCropOffsets.putAll(stripCropOffsets);

        // Determine displayScale
        double scale = resolveDisplayScale(spec);
        configWriter.withDisplayScale(scale);

        try {
            // sprite-atlas.json → entityDir (NOT animDir)
            // This matches the frontend URL: /assets/{type}/{name}/sprite-atlas.json
            Path configPath = configWriter.writeSpriteAtlas(
                    spec.entityName(), standaloneAnims, gridAnimDefs,
                    overlayAnimDefs, allCropOffsets, entityDir);
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
