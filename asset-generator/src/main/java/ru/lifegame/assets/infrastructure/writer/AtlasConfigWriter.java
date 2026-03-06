package ru.lifegame.assets.infrastructure.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.assets.domain.model.asset.AnimationSpec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Writes atlas-config.json metadata files for animation atlases.
 * <p>
 * Supports two formats:
 * <ul>
 *   <li><b>Strip config</b> — flat JSON for single-row atlases</li>
 *   <li><b>Grid config</b> — JSON with rows array for multi-row atlases (time-of-day variants)</li>
 * </ul>
 */
public class AtlasConfigWriter {

    private static final Logger log = LoggerFactory.getLogger(AtlasConfigWriter.class);

    /**
     * Writes a single atlas config entry as a JSON file.
     */
    public Path writeConfig(AnimationSpec spec, String atlasFileName, Path outputDir) throws IOException {
        String json = formatSingleEntry(spec, atlasFileName);
        Files.createDirectories(outputDir);
        Path configPath = outputDir.resolve(spec.name() + "-atlas-config.json");
        Files.writeString(configPath, json, StandardCharsets.UTF_8);
        log.info("Wrote atlas config: {}", configPath);
        return configPath;
    }

    /**
     * Writes a grid atlas config for animations grouped by condition (e.g. time_of_day).
     * <p>
     * Output format:
     * <pre>
     * {
     *   "name": "idle",
     *   "file": "idle_atlas.png",
     *   "layout": "grid",
     *   "frameWidth": 24,
     *   "frameHeight": 20,
     *   "columns": 4,
     *   "rows": [
     *     { "condition": { "type": "time_of_day", "value": "morning" }, "rowIndex": 0, "fps": 4, "loop": true },
     *     { "condition": { "type": "time_of_day", "value": "day" },     "rowIndex": 1, "fps": 4, "loop": true },
     *     ...
     *   ],
     *   "defaultRow": 0
     * }
     * </pre>
     *
     * @param baseName          base animation name (e.g. "idle")
     * @param atlasFileName     filename of the grid atlas PNG
     * @param conditionType     condition type (e.g. "time_of_day")
     * @param rowSpecs          ordered map: conditionValue → AnimationSpec for that row
     * @param outputDir         output directory
     * @return path to the generated config file
     */
    public Path writeGridConfig(String baseName, String atlasFileName, String conditionType,
                                LinkedHashMap<String, AnimationSpec> rowSpecs,
                                Path outputDir) throws IOException {
        var firstSpec = rowSpecs.values().iterator().next();

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"name\": \"").append(baseName).append("\",\n");
        sb.append("  \"file\": \"").append(atlasFileName).append("\",\n");
        sb.append("  \"layout\": \"grid\",\n");
        sb.append("  \"frameWidth\": ").append(firstSpec.frameWidth()).append(",\n");
        sb.append("  \"frameHeight\": ").append(firstSpec.frameHeight()).append(",\n");
        sb.append("  \"columns\": ").append(firstSpec.frames()).append(",\n");
        sb.append("  \"rows\": [\n");

        int rowIdx = 0;
        for (var entry : rowSpecs.entrySet()) {
            AnimationSpec spec = entry.getValue();
            sb.append("    { ");
            sb.append("\"condition\": { \"type\": \"").append(conditionType)
              .append("\", \"value\": \"").append(entry.getKey()).append("\" }, ");
            sb.append("\"rowIndex\": ").append(rowIdx).append(", ");
            sb.append("\"fps\": ").append(spec.fps()).append(", ");
            sb.append("\"loop\": ").append(spec.loop());
            sb.append(" }");
            if (rowIdx < rowSpecs.size() - 1) sb.append(",");
            sb.append("\n");
            rowIdx++;
        }

        sb.append("  ],\n");
        sb.append("  \"defaultRow\": 0\n");
        sb.append("}\n");

        Files.createDirectories(outputDir);
        Path configPath = outputDir.resolve(baseName + "-atlas-config.json");
        Files.writeString(configPath, sb.toString(), StandardCharsets.UTF_8);
        log.info("Wrote grid atlas config: {}", configPath);
        return configPath;
    }

    /**
     * Writes a combined atlas-config.json with entries for all animations.
     */
    public Path writeCombinedConfig(List<AnimationSpec> specs, Path outputDir) throws IOException {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < specs.size(); i++) {
            AnimationSpec spec = specs.get(i);
            sb.append(formatEntry(spec));
            if (i < specs.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");

        Files.createDirectories(outputDir);
        Path configPath = outputDir.resolve("atlas-config.json");
        Files.writeString(configPath, sb.toString(), StandardCharsets.UTF_8);
        log.info("Wrote combined atlas config: {}", configPath);
        return configPath;
    }

    public String formatSingleEntry(AnimationSpec spec, String atlasFileName) {
        return "{\n"
                + "  \"name\": \"" + spec.name() + "\",\n"
                + "  \"file\": \"" + atlasFileName + "\",\n"
                + "  \"layout\": \"strip\",\n"
                + "  \"frameWidth\": " + spec.frameWidth() + ",\n"
                + "  \"frameHeight\": " + spec.frameHeight() + ",\n"
                + "  \"frames\": " + spec.frames() + ",\n"
                + "  \"fps\": " + spec.fps() + ",\n"
                + "  \"loop\": " + spec.loop() + "\n"
                + "}";
    }

    private String formatEntry(AnimationSpec spec) {
        return "  {\n"
                + "    \"name\": \"" + spec.name() + "\",\n"
                + "    \"layout\": \"strip\",\n"
                + "    \"frameWidth\": " + spec.frameWidth() + ",\n"
                + "    \"frameHeight\": " + spec.frameHeight() + ",\n"
                + "    \"frames\": " + spec.frames() + ",\n"
                + "    \"fps\": " + spec.fps() + ",\n"
                + "    \"loop\": " + spec.loop() + "\n"
                + "  }";
    }
}
