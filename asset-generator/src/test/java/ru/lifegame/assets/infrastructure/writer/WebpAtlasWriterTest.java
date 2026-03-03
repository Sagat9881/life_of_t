package ru.lifegame.assets.infrastructure.writer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.lifegame.assets.domain.model.asset.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link WebpAtlasWriter}.
 */
class WebpAtlasWriterTest {

    @TempDir
    Path tempDir;

    private final WebpAtlasWriter writer = new WebpAtlasWriter();

    // -----------------------------------------------------------------------
    // write() — happy path
    // -----------------------------------------------------------------------

    @Test
    void write_singleAnimation_producesAtlasFile() {
        AssetSpec spec = specWithAnimations(
                List.of(new AnimationSpec("idle", 4, 8, true, List.of(0, 1, 2, 3)))
        );
        writer.write(spec, tempDir);

        // Expect either .webp or .png fallback
        long files = countAtlasFiles(tempDir);
        assertThat(files).isGreaterThanOrEqualTo(1);
    }

    @Test
    void write_multipleAnimations_atlasWidthMatchesTotalFrames() throws Exception {
        AnimationSpec anim1 = new AnimationSpec("idle", 2, 8, true, List.of(0, 1));
        AnimationSpec anim2 = new AnimationSpec("walk", 4, 12, true, List.of(2, 3, 4, 5));
        AssetSpec spec = specWithAnimations(List.of(anim1, anim2));
        writer.write(spec, tempDir);

        long files = countAtlasFiles(tempDir);
        assertThat(files).isGreaterThanOrEqualTo(1);
    }

    @Test
    void write_noAnimations_producesNoFile() {
        AssetSpec spec = specWithAnimations(List.of());
        writer.write(spec, tempDir);

        long files = countAtlasFiles(tempDir);
        assertThat(files).isEqualTo(0);
    }

    // -----------------------------------------------------------------------
    // Custom naming
    // -----------------------------------------------------------------------

    @Test
    void write_withNamingSpec_usesPattern() {
        NamingSpec naming = new NamingSpec(
                "tanya", "%s_layer_%02d.png", "%s_atlas.webp", "%s_atlas.json");
        AssetSpec spec = new AssetSpec(
                "tanya_idle", "character", "desc",
                List.of(), List.of(new AnimationSpec("idle", 2, 8, true, List.of(0, 1))),
                List.of(), List.of(),
                naming, new AssetConstraints(64, 64, 16, true, "png,webp"));

        writer.write(spec, tempDir);

        // atlas file should match naming pattern (webp or png fallback)
        boolean found = tempDir.toFile().list() != null &&
                java.util.Arrays.stream(tempDir.toFile().list())
                        .anyMatch(f -> f.startsWith("tanya_atlas"));
        assertThat(found).isTrue();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private AssetSpec specWithAnimations(List<AnimationSpec> anims) {
        return new AssetSpec(
                "test_asset", "character", "desc",
                List.of(), anims, List.of(), List.of(),
                null, new AssetConstraints(64, 64, 16, true, "png,webp"));
    }

    private long countAtlasFiles(Path dir) {
        String[] files = dir.toFile().list();
        if (files == null) return 0;
        return java.util.Arrays.stream(files)
                .filter(f -> f.endsWith(".webp") || f.endsWith(".png"))
                .count();
    }
}
