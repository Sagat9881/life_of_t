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
 * Output conforms to {@link AtlasConfigSchema} (config version 2.0).
 * <p>
 * All animations are grid-only. An animation without explicit variant rows
 * gets a single-row grid with condition { "all": [] } (always true / default).
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
     * Writes sprite-atlas.json (v2.0) to {@code outputDir/sprite-atlas.json}.
     *
     * @param entityName  entity name (e.g. "tanya")
     * @param gridAnims   map of animation name → GridAnimDef
     * @param overlayAnims map of overlay layer id → OverlayAnimDef
     * @param cropOffsets  map of animation name → CropOffsetDef (may be null)
     * @param outputDir   entity root directory (sprite-atlas.json written here)
     * @return path to the written config file
     */
    public Path writeSpriteAtlas(
            String entityName,
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

    // -------------------------------------------------------------------------
    // Private formatters
    // -------------------------------------------------------------------------

    private String formatCropOffset(CropOffsetDef crop) {
        if (crop == null) return "";
        return "      \"cropOffset\": {\n"
                + "        \"x\": " + crop.x() + ",\n"
                + "        \"y\": " + crop.y() + ",\n"
                + "        \"originalWidth\": " + crop.originalWidth() + ",\n"
                + "        \"originalHeight\": " + crop.originalHeight() + "\n"
                + "      },\n";
    }

    /**
     * Serialises a list of SingleCondition predicates as a JSON array.
     * Numbers are written without quotes; all other values are quoted strings.
     */
    private String formatConditionArray(List<AtlasConfigSchema.SingleCondition> all) {
        if (all == null || all.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < all.size(); i++) {
            AtlasConfigSchema.SingleCondition c = all.get(i);
            sb.append("              {");
            sb.append(" \"space\": \"").append(c.space()).append("\",");
            if (c.npcId() != null) {
                sb.append(" \"npcId\": \"").append(c.npcId()).append("\",");
            }
            sb.append(" \"stat\": \"").append(c.stat()).append("\",");
            sb.append(" \"operator\": \"").append(c.operator()).append("\",");
            if (c.value() instanceof Number) {
                sb.append(" \"value\": ").append(c.value());
            } else {
                sb.append(" \"value\": \"").append(c.value()).append("\"");
            }
            sb.append(" }");
            if (i < all.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("            ]");
        return sb.toString();
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

        AnimationSpec frameSpec = def.frameSpec();
        int fw = crop != null ? crop.croppedWidth() : frameSpec.frameWidth();
        int fh = crop != null ? crop.croppedHeight() : frameSpec.frameHeight();
        sb.append("      \"columns\": ").append(frameSpec.frames()).append(",\n");
        sb.append("      \"frameWidth\": ").append(fw).append(",\n");
        sb.append("      \"frameHeight\": ").append(fh).append(",\n");
        sb.append("      \"rows\": [\n");

        List<AtlasConfigSchema.RowDef> rows = def.rows();
        for (int i = 0; i < rows.size(); i++) {
            AtlasConfigSchema.RowDef row = rows.get(i);
            sb.append("        {\n");
            sb.append("          \"rowIndex\": ").append(row.rowIndex()).append(",\n");
            List<AtlasConfigSchema.SingleCondition> all =
                    row.condition() != null ? row.condition().all() : List.of();
            sb.append("          \"condition\": { \"all\": ").append(formatConditionArray(all)).append(" },\n");
            sb.append("          \"fps\": ").append(row.fps()).append(",\n");
            sb.append("          \"loop\": ").append(row.loop()).append("\n");
            sb.append("        }");
            if (i < rows.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("      ],\n");
        // defaultRow: last row index in list that has empty condition, else 0
        int defaultRow = rows.isEmpty() ? 0 : rows.size() - 1;
        for (int i = 0; i < rows.size(); i++) {
            AtlasConfigSchema.RowDef r = rows.get(i);
            if (r.condition() != null && r.condition().all() != null && r.condition().all().isEmpty()) {
                defaultRow = r.rowIndex();
                break;
            }
        }
        sb.append("      \"defaultRow\": ").append(defaultRow).append("\n");
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
            List<AtlasConfigSchema.SingleCondition> all = row.condition() != null ? row.condition() : List.of();
            sb.append("          \"condition\": { \"all\": ").append(formatConditionArray(all)).append(" },\n");
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

    // -------------------------------------------------------------------------
    // Public data records
    // -------------------------------------------------------------------------

    /**
     * Grid animation definition.
     *
     * @param rows      ordered list of row definitions (each carries its own condition and fps/loop)
     * @param frameSpec animation spec that carries frame dimensions and count
     */
    public record GridAnimDef(
            List<AtlasConfigSchema.RowDef> rows,
            AnimationSpec frameSpec
    ) {}

    public record OverlayAnimDef(
            int frameWidth,
            int frameHeight,
            List<OverlayRowDef> rows,
            int defaultRow
    ) {}

    /**
     * Overlay row definition.
     *
     * @param condition list of SingleCondition predicates for this row
     * @param tint      tint hex colour (e.g. "#FFFFFF")
     * @param opacity   opacity 0.0–1.0
     */
    public record OverlayRowDef(
            List<AtlasConfigSchema.SingleCondition> condition,
            String tint,
            double opacity
    ) {}

    /**
     * Stores the crop offset and dimensions for a cropped animation.
     *
     * @param x              X offset of crop region in original canvas
     * @param y              Y offset of crop region in original canvas
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
    ) {}
}
