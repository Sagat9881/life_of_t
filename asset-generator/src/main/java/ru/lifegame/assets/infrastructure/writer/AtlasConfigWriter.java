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
 * <p>
 * Output conforms to {@link AtlasConfigSchema} (config version 1.2).
 * <p>
 * v1.2 changes:
 * <ul>
 *   <li>Added renderMode ("sprite" | "overlay") to animation entries</li>
 *   <li>Added tint/opacity to row definitions for overlay mode</li>
 *   <li>Renamed top-level "character" to "entity"</li>
 * </ul>
 */
public class AtlasConfigWriter {

    private static final Logger log = LoggerFactory.getLogger(AtlasConfigWriter.class);

    private String revision = AtlasConfigSchema.LATEST_REVISION;

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

    /**
     * Writes the unified sprite-atlas.json.
     *
     * @param entityName   entity identifier (e.g. "sam", "home_room")
     * @param stripAnims   list of simple strip animations (walk, eat, etc.)
     * @param gridAnims    map of baseName → grid animation definition
     * @param overlayAnims map of baseName → overlay animation definition
     * @param outputDir    target directory
     * @return path to generated sprite-atlas.json
     */
    public Path writeSpriteAtlas(
            String entityName,
            List<AnimationSpec> stripAnims,
            Map<String, GridAnimDef> gridAnims,
            Map<String, OverlayAnimDef> overlayAnims,
            Path outputDir
    ) throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"configVersion\": \"").append(AtlasConfigSchema.CURRENT_VERSION).append("\",\n");
        sb.append("  \"revision\": \"").append(revision).append("\",\n");
        sb.append("  \"entity\": \"").append(entityName).append("\",\n");
        sb.append("  \"animations\": {\n");

        List<String> entries = new ArrayList<>();

        // Overlay animations (e.g. ambient_light)
        if (overlayAnims != null) {
            for (var oe : overlayAnims.entrySet()) {
                entries.add(formatOverlayEntry(oe.getKey(), oe.getValue()));
            }
        }

        // Grid animations (e.g. idle with time-of-day rows)
        if (gridAnims != null) {
            for (var ge : gridAnims.entrySet()) {
                entries.add(formatGridEntry(ge.getKey(), ge.getValue()));
            }
        }

        // Strip animations (e.g. walk, eat)
        if (stripAnims != null) {
            for (AnimationSpec spec : stripAnims) {
                entries.add(formatStripEntry(spec));
            }
        }

        sb.append(String.join(",\n", entries));
        sb.append("\n  }\n");
        sb.append("}\n");

        Files.createDirectories(outputDir);
        Path configPath = outputDir.resolve("sprite-atlas.json");
        Files.writeString(configPath, sb.toString(), StandardCharsets.UTF_8);
        log.info("Wrote sprite-atlas.json (v{}, rev {}): {}",
                AtlasConfigSchema.CURRENT_VERSION, revision, configPath);
        return configPath;
    }

    /** Backward-compat overload without overlayAnims. */
    public Path writeSpriteAtlas(
            String entityName,
            List<AnimationSpec> stripAnims,
            Map<String, GridAnimDef> gridAnims,
            Path outputDir
    ) throws IOException {
        return writeSpriteAtlas(entityName, stripAnims, gridAnims, null, outputDir);
    }

    private String formatStripEntry(AnimationSpec spec) {
        return "    \"" + spec.name() + "\": {\n"
                + "      \"file\": \"" + spec.name() + "_atlas.png\",\n"
                + "      \"layout\": \"strip\",\n"
                + "      \"renderMode\": \"sprite\",\n"
                + "      \"columns\": " + spec.frames() + ",\n"
                + "      \"frameWidth\": " + spec.frameWidth() + ",\n"
                + "      \"frameHeight\": " + spec.frameHeight() + ",\n"
                + "      \"fps\": " + spec.fps() + ",\n"
                + "      \"loop\": " + spec.loop() + "\n"
                + "    }";
    }

    private String formatGridEntry(String baseName, GridAnimDef def) {
        StringBuilder sb = new StringBuilder();
        sb.append("    \"").append(baseName).append("\": {\n");
        sb.append("      \"file\": \"").append(baseName).append("_atlas.png\",\n");
        sb.append("      \"layout\": \"grid\",\n");
        sb.append("      \"renderMode\": \"sprite\",\n");

        var firstSpec = def.rowSpecs().values().iterator().next();
        sb.append("      \"columns\": ").append(firstSpec.frames()).append(",\n");
        sb.append("      \"frameWidth\": ").append(firstSpec.frameWidth()).append(",\n");
        sb.append("      \"frameHeight\": ").append(firstSpec.frameHeight()).append(",\n");
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

    /**
     * Grid animation group (sprite renderMode).
     */
    public record GridAnimDef(
            String conditionType,
            LinkedHashMap<String, AnimationSpec> rowSpecs
    ) {}

    /**
     * Overlay animation group — e.g. ambient_light with tint/opacity per condition.
     */
    public record OverlayAnimDef(
            int frameWidth,
            int frameHeight,
            List<OverlayRowDef> rows,
            int defaultRow
    ) {}

    /**
     * Single row in an overlay animation.
     */
    public record OverlayRowDef(
            String conditionValue,
            String tint,
            double opacity
    ) {}
}
