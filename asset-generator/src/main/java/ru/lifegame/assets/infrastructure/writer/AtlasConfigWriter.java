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
 * Output conforms to {@link AtlasConfigSchema} (config version 1.0).
 * <p>
 * Features:
 * <ul>
 *   <li>Single file per character containing ALL animation entries</li>
 *   <li>Schema version ({@code configVersion}) for frontend compatibility checks</li>
 *   <li>Revision field — defaults to "latest" for dev, can be pinned for production</li>
 *   <li>Supports both strip (1 row) and grid (N rows with conditions) layouts</li>
 * </ul>
 */
public class AtlasConfigWriter {

    private static final Logger log = LoggerFactory.getLogger(AtlasConfigWriter.class);

    private String revision = AtlasConfigSchema.LATEST_REVISION;

    /**
     * Set a specific revision tag. Pass null or "latest" for default behavior.
     */
    public AtlasConfigWriter withRevision(String revision) {
        this.revision = (revision == null || revision.isBlank())
                ? AtlasConfigSchema.LATEST_REVISION
                : revision;
        return this;
    }

    /**
     * Generate a timestamp-based revision string (e.g. "20260306-1").
     */
    public AtlasConfigWriter withTimestampRevision() {
        this.revision = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-1";
        return this;
    }

    /**
     * Writes the unified sprite-atlas.json for a character.
     *
     * @param characterName   character identifier (e.g. "sam")
     * @param spriteWidth     display width of one frame
     * @param spriteHeight    display height of one frame
     * @param stripAnims      list of simple strip animations (walk, eat, tremble, etc.)
     * @param gridAnims       map of baseName → (conditionType, orderedRowSpecs)
     * @param outputDir       target directory
     * @return path to generated sprite-atlas.json
     */
    public Path writeSpriteAtlas(
            String characterName,
            int spriteWidth,
            int spriteHeight,
            List<AnimationSpec> stripAnims,
            Map<String, GridAnimDef> gridAnims,
            Path outputDir
    ) throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"configVersion\": \"").append(AtlasConfigSchema.CURRENT_VERSION).append("\",\n");
        sb.append("  \"revision\": \"").append(revision).append("\",\n");
        sb.append("  \"character\": \"").append(characterName).append("\",\n");
        sb.append("  \"spriteWidth\": ").append(spriteWidth).append(",\n");
        sb.append("  \"spriteHeight\": ").append(spriteHeight).append(",\n");
        sb.append("  \"animations\": {\n");

        List<String> entries = new ArrayList<>();

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

    private String formatStripEntry(AnimationSpec spec) {
        return "    \"" + spec.name() + "\": {\n"
                + "      \"file\": \"" + spec.name() + "_atlas.png\",\n"
                + "      \"layout\": \"strip\",\n"
                + "      \"columns\": " + spec.frames() + ",\n"
                + "      \"fps\": " + spec.fps() + ",\n"
                + "      \"loop\": " + spec.loop() + "\n"
                + "    }";
    }

    private String formatGridEntry(String baseName, GridAnimDef def) {
        StringBuilder sb = new StringBuilder();
        sb.append("    \"").append(baseName).append("\": {\n");
        sb.append("      \"file\": \"").append(baseName).append("_atlas.png\",\n");
        sb.append("      \"layout\": \"grid\",\n");

        var firstSpec = def.rowSpecs().values().iterator().next();
        sb.append("      \"columns\": ").append(firstSpec.frames()).append(",\n");
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

    /**
     * Definition of a grid animation group.
     *
     * @param conditionType condition type (e.g. "time_of_day")
     * @param rowSpecs      ordered map: conditionValue → AnimationSpec per row
     */
    public record GridAnimDef(
            String conditionType,
            LinkedHashMap<String, AnimationSpec> rowSpecs
    ) {}
}
