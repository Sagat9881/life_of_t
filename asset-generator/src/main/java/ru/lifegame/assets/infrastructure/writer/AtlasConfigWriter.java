package ru.lifegame.assets.infrastructure.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.assets.domain.model.asset.AnimationSpec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Writes atlas-config.json metadata files for animation atlases.
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
     * Writes a combined atlas-config.json with entries for all animations.
     */
    public Path writeCombinedConfig(List<AnimationSpec> specs, Path outputDir) throws IOException {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < specs.size(); i++) {
            AnimationSpec spec = specs.get(i);
            sb.append(formatEntry(spec));
            if (i < specs.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("]");

        Files.createDirectories(outputDir);
        Path configPath = outputDir.resolve("atlas-config.json");
        Files.writeString(configPath, sb.toString(), StandardCharsets.UTF_8);
        log.info("Wrote combined atlas config: {}", configPath);
        return configPath;
    }

    /**
     * Formats a single entry as a standalone JSON object string.
     */
    public String formatSingleEntry(AnimationSpec spec, String atlasFileName) {
        return "{\n"
                + "  \"name\": \"" + spec.name() + "\",\n"
                + "  \"file\": \"" + atlasFileName + "\",\n"
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
                + "    \"frameWidth\": " + spec.frameWidth() + ",\n"
                + "    \"frameHeight\": " + spec.frameHeight() + ",\n"
                + "    \"frames\": " + spec.frames() + ",\n"
                + "    \"fps\": " + spec.fps() + ",\n"
                + "    \"loop\": " + spec.loop() + "\n"
                + "  }";
    }
}
