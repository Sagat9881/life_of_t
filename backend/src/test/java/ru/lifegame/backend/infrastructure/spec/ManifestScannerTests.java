package ru.lifegame.backend.infrastructure.spec;

import ru.lifegame.backend.domain.narrative.spec.BlockManifest;
import ru.lifegame.backend.domain.narrative.spec.SpecEntry;
import ru.lifegame.backend.domain.narrative.spec.SpecLoadException;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link ManifestScanner}.
 *
 * <p>Uses an in-memory stub of {@link PathMatchingResourcePatternResolver}
 * to avoid classpath dependencies in CI.
 *
 * <p>Ref: java-developer-skill.md §5.5, TASK-BE-016.
 */
class ManifestScannerTests {

    // ── helper: write a manifest XML to a temp file ──────────────────────────

    private Path writeManifest(Path dir, String blockId, String... entries) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<manifest block-id=\"").append(blockId).append("\" version=\"1.0\">\n");
        for (String e : entries) sb.append(e).append("\n");
        sb.append("</manifest>\n");
        Path file = dir.resolve("manifest.xml");
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
        return file;
    }

    // ── stub resolver ────────────────────────────────────────────────────────

    /**
     * Creates a resolver stub whose {@code getResources()} returns exactly the
     * provided file-system resources, ignoring the pattern string.
     */
    private PathMatchingResourcePatternResolver stubResolver(Path... files) {
        return new PathMatchingResourcePatternResolver() {
            @Override
            public Resource[] getResources(String locationPattern) {
                Resource[] res = new Resource[files.length];
                for (int i = 0; i < files.length; i++) res[i] = new FileSystemResource(files[i]);
                return res;
            }
        };
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Nested
    class ScanAllTests {

        @Test
        void empty_directory_returns_empty_list(@TempDir Path tmp) throws Exception {
            ManifestScanner scanner = new ManifestScanner(
                    stubResolver(), new XmlManifestParser());
            assertThat(scanner.scanAll()).isEmpty();
        }

        @Test
        void single_manifest_with_two_entries(@TempDir Path tmp) throws Exception {
            Path manifest = writeManifest(tmp, "quest",
                    "<entry entity-id=\"q1\" entity-type=\"quest\" spec-path=\"narrative/quest/q1/spec.xml\"/>",
                    "<entry entity-id=\"q2\" entity-type=\"quest\" spec-path=\"narrative/quest/q2/spec.xml\"/>"
            );
            ManifestScanner scanner = new ManifestScanner(
                    stubResolver(manifest), new XmlManifestParser());

            List<BlockManifest> result = scanner.scanAll();

            assertThat(result).hasSize(1);
            BlockManifest m = result.get(0);
            assertThat(m.blockId()).isEqualTo("quest");
            assertThat(m.entries()).hasSize(2);
            assertThat(m.entries().get(0).entityId()).isEqualTo("q1");
            assertThat(m.entries().get(1).entityId()).isEqualTo("q2");
        }

        @Test
        void multiple_blocks_discovered(@TempDir Path tmp) throws Exception {
            Path dirA = Files.createDirectory(tmp.resolve("quest"));
            Path dirB = Files.createDirectory(tmp.resolve("npc"));
            Path mA = writeManifest(dirA, "quest");
            Path mB = writeManifest(dirB, "npc");

            ManifestScanner scanner = new ManifestScanner(
                    stubResolver(mA, mB), new XmlManifestParser());

            List<BlockManifest> result = scanner.scanAll();
            assertThat(result).extracting(BlockManifest::blockId)
                    .containsExactlyInAnyOrder("quest", "npc");
        }

        @Test
        void malformed_manifest_throws_SpecLoadException(@TempDir Path tmp) throws Exception {
            Path bad = tmp.resolve("manifest.xml");
            Files.writeString(bad, "THIS IS NOT XML", StandardCharsets.UTF_8);

            ManifestScanner scanner = new ManifestScanner(
                    stubResolver(bad), new XmlManifestParser());

            assertThatThrownBy(scanner::scanAll)
                    .isInstanceOf(SpecLoadException.class);
        }

        @Test
        void no_hardcoded_block_names_in_pattern() {
            // Guard: the pattern must not mention any specific block name.
            String pattern = ManifestScanner.MANIFEST_PATTERN;
            assertThat(pattern).doesNotContain("quest");
            assertThat(pattern).doesNotContain("npc");
            assertThat(pattern).doesNotContain("conflicts");
            assertThat(pattern).doesNotContain("world-events");
        }
    }

    @Nested
    class GetManifestTests {

        @Test
        void returns_manifest_for_existing_block_id(@TempDir Path tmp) throws Exception {
            Path m = writeManifest(tmp, "npc",
                    "<entry entity-id=\"aijan\" entity-type=\"npc\" spec-path=\"narrative/npc/aijan.xml\"/>");
            ManifestScanner scanner = new ManifestScanner(
                    stubResolver(m), new XmlManifestParser());

            BlockManifest result = scanner.getManifest("npc");
            assertThat(result.blockId()).isEqualTo("npc");
            assertThat(result.entries()).hasSize(1);
        }

        @Test
        void throws_for_unknown_block_id(@TempDir Path tmp) throws Exception {
            Path m = writeManifest(tmp, "quest");
            ManifestScanner scanner = new ManifestScanner(
                    stubResolver(m), new XmlManifestParser());

            assertThatThrownBy(() -> scanner.getManifest("npc"))
                    .isInstanceOf(SpecLoadException.class)
                    .hasMessageContaining("npc");
        }
    }
}
