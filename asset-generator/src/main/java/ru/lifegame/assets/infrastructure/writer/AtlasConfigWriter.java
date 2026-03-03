package ru.lifegame.assets.infrastructure.writer;

import org.springframework.stereotype.Component;
import ru.lifegame.assets.domain.model.asset.AnimationSpec;
import ru.lifegame.assets.domain.model.asset.AssetSpec;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Writes a JSON atlas-configuration file that describes the frame layout
 * of the generated WebP sprite atlas.
 *
 * <p>The output format is a deliberately minimal JSON structure that can
 * be consumed by Phaser 3, Godot, or any custom engine loader without
 * additional processing.</p>
 *
 * <h2>Output example</h2>
 * <pre>{@code
 * {
 *   "asset": "tanya_idle",
 *   "frameWidth": 64,
 *   "frameHeight": 64,
 *   "totalFrames": 8,
 *   "animations": [
 *     { "state": "idle", "frameCount": 4, "fps": 8, "loop": true, "frames": [0,1,2,3] },
 *     { "state": "walk", "frameCount": 4, "fps": 12, "loop": true, "frames": [4,5,6,7] }
 *   ]
 * }
 * }</pre>
 */
@Component
public class AtlasConfigWriter {

    /**
     * Write a JSON config for {@code spec} into {@code outputDir}.
     *
     * @param spec      asset specification
     * @param outputDir target directory; created if absent
     * @throws ConfigWriteException on any I/O error
     */
    public void write(AssetSpec spec, Path outputDir) {
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            throw new ConfigWriteException("Cannot create output directory: " + outputDir, e);
        }

        Path dest = outputDir.resolve(configFileName(spec));
        try (PrintWriter pw = new PrintWriter(
                Files.newBufferedWriter(dest, StandardCharsets.UTF_8))) {
            writeJson(pw, spec);
        } catch (IOException e) {
            throw new ConfigWriteException("Failed to write atlas config: " + dest, e);
        }
    }

    // -----------------------------------------------------------------------
    // JSON generation (no external library dependency)
    // -----------------------------------------------------------------------

    private void writeJson(PrintWriter pw, AssetSpec spec) {
        int frameW = spec.constraints() != null ? spec.constraints().widthPx()  : 64;
        int frameH = spec.constraints() != null ? spec.constraints().heightPx() : 64;
        int total  = spec.animations().stream().mapToInt(AnimationSpec::frameCount).sum();

        pw.println("{");
        pw.printf("  \"asset\": \"%s\",%n",         escape(spec.id()));
        pw.printf("  \"frameWidth\": %d,%n",         frameW);
        pw.printf("  \"frameHeight\": %d,%n",        frameH);
        pw.printf("  \"totalFrames\": %d,%n",        total);
        pw.println("  \"animations\": [");

        List<AnimationSpec> anims = spec.animations();
        for (int i = 0; i < anims.size(); i++) {
            AnimationSpec a = anims.get(i);
            String framesJson = a.frames().stream()
                    .map(Object::toString)
                    .reduce((x, y) -> x + "," + y)
                    .orElse("");
            pw.printf("    { \"state\": \"%s\", \"frameCount\": %d, \"fps\": %d, "
                     + "\"loop\": %b, \"frames\": [%s] }%s%n",
                     escape(a.state()), a.frameCount(), a.fps(), a.loop(),
                     framesJson,
                     i < anims.size() - 1 ? "," : "");
        }

        pw.println("  ]");
        pw.println("}");
    }

    private String configFileName(AssetSpec spec) {
        if (spec.naming() != null && spec.naming().configPattern() != null) {
            return String.format(spec.naming().configPattern(), spec.naming().prefix());
        }
        return spec.id() + "_atlas.json";
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // -----------------------------------------------------------------------
    // Exception
    // -----------------------------------------------------------------------

    public static class ConfigWriteException extends RuntimeException {
        public ConfigWriteException(String msg, Throwable cause) { super(msg, cause); }
    }
}
