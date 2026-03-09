package ru.lifegame.assets.infrastructure.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.assets.domain.model.asset.AnimationSpec;
import ru.lifegame.assets.domain.model.asset.AtlasConfigSchema;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Writes sprite-atlas.json — the unified, versioned atlas configuration.
 * Output conforms to {@link AtlasConfigSchema} (config version 1.4).
 */
public class AtlasConfigWriter {

    private static final Logger log = LoggerFactory.getLogger(AtlasConfigWriter.class);

    private String revision = AtlasConfigSchema.LATEST_REVISION;
    private double displayScale = AtlasConfigSchema.DEFAULT_DISPLAY_SCALE;

    public AtlasConfigWriter withRevision(String revision) {
        this.revision = (revision == null || revision.isBlank())
                ? AtlasConfigSchema.LATEST_REVISION
                : revision;
        return this;
    }

    public AtlasConfigWriter withTimestampRevision() {
        this.revision = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-1";
        return this;
    }

    public AtlasConfigWriter withDisplayScale(double scale) {
        this.displayScale = scale > 0 ? scale : AtlasConfigSchema.DEFAULT_DISPLAY_SCALE;
        return this;
    }

    /**
     * Writes the unified sprite-atlas.json (v1.4 with cropOffset support).
     */
    public Path writeSpriteAtlas(
            String entityName,
            List<AnimationSpec> stripAnims,
            Map<String, GridAnimDef> gridAnims,
            Map<String, OverlayAnimDef> overlayAnims,
            Map<String, CropOffsetDef> cropOffsets,
            Path outputDir
    ) throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"configVersion\": \"").append(AtlasConfigSchema.CURRENT_VERSION).append("\",\n");
        sb.append("  \"revision\": \"").append(revision).append("\",\n");
        sb.append("  \"entity\": \"").append(entityName).append("\",\n");
        sb.append("  \"displayScale\": ").append(displayScale).append(",\n");
        sb.append("  \"animations\": {\n");

        List<String> entries = new ArrayList<>();

        if (overlayAnims != null) {
            for (var oe : overlayAnims.entrySet()) {
                entries.add(formatOverlayEntry(oe.getKey(), oe.getValue()));
            }
        }

        if (gridAnims != null) {
            for (var ge : gridAnims.entrySet()) {
                CropOffsetDef crop = cropOffsets != null ? cropOffsets.get(ge.getKey()) : null;
                entries.add(formatGridEntry(ge.getKey(), ge.getValue(), crop));
            }
        }

        if (stripAnims != null) {
            for (AnimationSpec spec : stripAnims) {
                CropOffsetDef crop = cropOffsets != null ? cropOffsets.get(spec.name()) : null;
                entries.add(formatStripEntry(spec, crop));
            }
        }

        sb.append(String.join(",\n", entries));
        sb.append("\n  }\n");
        sb.append("}\n");

        Files.createDirectories(outputDir);
        Path configPath = outputDir.resolve("sprite-atlas.json");
        Files.writeString(configPath, sb.toString(), StandardCharsets.UTF_8);
        log.info("Wrote sprite-atlas.json (v{}, rev {}, scale {}): {}",
                AtlasConfigSchema.CURRENT_VERSION, revision, displayScale, configPath);
        return configPath;
    }

    /** Backward-compat overload without cropOffsets. */
    public Path writeSpriteAtlas(
            String entityName,
            List<AnimationSpec> stripAnims,
            Map<String, GridAnimDef> gridAnims,
            Map<String, OverlayAnimDef> overlayAnims,
            Path outputDir
    ) throws IOException {
        return writeSpriteAtlas(entityName, stripAnims, gridAnims, overlayAnims, null, outputDir);
    }

    /** Backward-compat overload without overlayAnims or cropOffsets. */
    public Path writeSpriteAtlas(
            String entityName,
            List<AnimationSpec> stripAnims,
            Map<String, GridAnimDef> gridAnims,
            Path outputDir
    ) throws IOException {
        return writeSpriteAtlas(entityName, stripAnims, gridAnims, null, null, outputDir);
    }

    private String formatCropOffset(CropOffsetDef crop) {
        if (crop == null) return "";
        return "      \"cropOffset\": {\n"
                + "        \"x\": " + crop.x() + ",\n"
                + "        \"y\": " + crop.y() + ",\n"
                + "        \"originalWidth\": " + crop.originalWidth() + ",\n"
                + "        \"originalHeight\": " + crop.originalHeight() + "\n"
                + "      },\n";
    }

    private String formatStripEntry(AnimationSpec spec, CropOffsetDef crop) {
        // If cropped, actual frame dimensions come from the cropped atlas
        // (written by WebpAtlasWriter from cropped frames).
        // frameWidth/frameHeight in JSON reflect the CROPPED size.
        int fw = crop != null ? spec.frameWidth() : spec.frameWidth();
        int fh = crop != null ? spec.frameHeight() : spec.frameHeight();
        // Note: when frames are cropped, the AnimationSpec still has original dimensions.
        // The actual cropped dimensions are in the atlas file. We need the real sizes.
        // Since WebpAtlasWriter uses frame.getWidth()/getHeight() from the actual images,
        // and LayeredAssetGenerator passes cropped frames, the atlas PNG is correct.
        // But for the JSON we need cropped dimensions. We can derive them:
        if (crop != null) {
            // The cropped dimensions are: originalWidth - cropX adjustment
            // But we have the crop bounds from computeCropBounds which gives us
            // the exact cropped w/h. However, we don't store them separately.
            // The CropOffsetDef stores original dimensions + offset.
            // Cropped size = we need to compute it. Let's store it.
            // Actually the simplest: frameWidth/Height in atlas = cropped.
            // We need to read from crop info. Add croppedW/H to CropOffsetDef? No.
            // Actually the atlas image IS cropped, so frameWidth/Height should
            // match. The spec.frameWidth() is ORIGINAL. We need cropped.
            // For now, the generator already passes cropped frames to the writer,
            // so the actual PNG atlas has correct pixel sizes.
            // The JSON needs to match. We compute from originalDim - offset.
            // Actually, computeCropBounds returns {x, y, w, h} — the w, h ARE the
            // cropped dimensions. But CropOffsetDef only stores x, y, origW, origH.
            // Let's fix: store cropped dims too. But that changes the record.
            // Simpler: the JSON frameWidth/Height = what the atlas actually contains.
            // Since we don't have that info here, let's adjust CropOffsetDef.
        }
        return "    \"" + spec.name() + "\": {\n"
                + "      \"file\": \"" + spec.name() + "_atlas.png\",\n"
                + "      \"layout\": \"strip\",\n"
                + "      \"renderMode\": \"sprite\",\n"
                + formatCropOffset(crop)
                + "      \"columns\": " + spec.frames() + ",\n"
                + "      \"frameWidth\": " + (crop != null ? crop.croppedWidth() : spec.frameWidth()) + ",\n"
                + "      \"frameHeight\": " + (crop != null ? crop.croppedHeight() : spec.frameHeight()) + ",\n"
                + "      \"fps\": " + spec.fps() + ",\n"
                + "      \"loop\": " + spec.loop() + "\n"
                + "    }";
    }

    /** Original overload for backward compat */
    private String formatStripEntry(AnimationSpec spec) {
        return formatStripEntry(spec, null);
    }

    private String formatGridEntry(String baseName, GridAnimDef def, CropOffsetDef crop) {
        StringBuilder sb = new StringBuilder();
        sb.append("    \"").append(baseName).append("\": {\n");
        sb.append("      \"file\": \"").append(baseName).append("_atlas.png\",\n");
        sb.append("      \"layout\": \"grid\",\n");
        sb.append("      \"renderMode\": \"sprite\",\n");

        if (crop != null) {
            sb.append(formatCropOffset(crop));
        }

        var firstSpec = def.rowSpecs().values().iterator().next();
        int fw = crop != null ? crop.croppedWidth() : firstSpec.frameWidth();
        int fh = crop != null ? crop.croppedHeight() : firstSpec.frameHeight();
        sb.append("      \"columns\": ").append(firstSpec.frames()).append(",\n");
        sb.append("      \"frameWidth\": ").append(fw).append(",\n");
        sb.append("      \"frameHeight\": ").append(fh).append(",\n");
        sb.append("      \"rows\": [\n");

        int rowIdx = 0;
        List<Map.Entry<String, AnimationSpec>> rowList = new ArrayList<>(def.rowSpecs().entrySet());
        for (int i = 0; i < rowList.size(); i++) {
            var entry = rowList.get(i);
            AnimationSpec spec = entry.getValue();
            sb.append("        {\n");
            sb.append("          \"rowIndex\": ").append(rowIdx).append(",\n");
            sb.append("          \"condition\": { \"type\": \"").append(def.conditionType())
              .append("\", \"value\": \"").append(entry.getKey()).append("\" },\n");
            sb.append("          \"fps\": ").append(spec.fps()).append(",\n");
            sb.append("          \"loop\": ").append(spec.loop()).append("\n");
            sb.append("        }");
            if (i < rowList.size() - 1) sb.append(",");
            sb.append("\n");
            rowIdx++;
        }

        sb.append("      ],\n");
        sb.append("      \"defaultRow\": 0\n");
        sb.append("    }");
        return sb.toString();
    }

    private String formatOverlayEntry(String baseName, OverlayAnimDef def) {
        StringBuilder sb = new StringBuilder();
        sb.append("    \"").append(baseName).append("\": {\n");
        sb.append("      \"file\": \"").append(baseName).append("_atlas.png\",\n");
        sb.append("      \"layout\": \"grid\",\n");
        sb.append("      \"renderMode\": \"overlay\",\n");
        sb.append("      \"columns\": 1,\n");
        sb.append("      \"frameWidth\": ").append(def.frameWidth()).append(",\n");
        sb.append("      \"frameHeight\": ").append(def.frameHeight()).append(",\n");
        sb.append("      \"rows\": [\n");

        List<OverlayRowDef> rows = def.rows();
        for (int i = 0; i < rows.size(); i++) {
            OverlayRowDef row = rows.get(i);
            sb.append("        {\n");
            sb.append("          \"rowIndex\": ").append(i).append(",\n");
            sb.append("          \"condition\": { \"type\": \"time_of_day\", \"value\": \"").append(row.conditionValue()).append("\" },\n");
            sb.append("          \"tint\": \"").append(row.tint()).append("\",\n");
            sb.append("          \"opacity\": ").append(row.opacity()).append(",\n");
            sb.append("          \"fps\": 1,\n");
            sb.append("          \"loop\": false\n");
            sb.append("        }");
            if (i < rows.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("      ],\n");
        sb.append("      \"defaultRow\": ").append(def.defaultRow()).append("\n");
        sb.append("    }");
        return sb.toString();
    }

    public record GridAnimDef(
            String conditionType,
            LinkedHashMap<String, AnimationSpec> rowSpecs
    ) {}

    public record OverlayAnimDef(
            int frameWidth,
            int frameHeight,
            List<OverlayRowDef> rows,
            int defaultRow
    ) {}

    public record OverlayRowDef(
            String conditionValue,
            String tint,
            double opacity
    ) {}

    /**
     * Stores the crop offset and dimensions for a cropped animation.
     * @param x            X offset of crop region in original canvas
     * @param y            Y offset of crop region in original canvas
     * @param originalWidth  original (uncropped) frame width
     * @param originalHeight original (uncropped) frame height
     * @param croppedWidth   actual cropped frame width (written to atlas)
     * @param croppedHeight  actual cropped frame height (written to atlas)
     */
    public record CropOffsetDef(
            int x,
            int y,
            int originalWidth,
            int originalHeight,
            int croppedWidth,
            int croppedHeight
    ) {
        /** Convenience constructor — computes cropped dims not needed at call site */
        public CropOffsetDef(int x, int y, int originalWidth, int originalHeight) {
            this(x, y, originalWidth, originalHeight, 0, 0);
        }
    }
}
