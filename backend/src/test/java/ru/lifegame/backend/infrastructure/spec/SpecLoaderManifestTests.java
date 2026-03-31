package ru.lifegame.backend.infrastructure.spec;

import ru.lifegame.backend.domain.narrative.spec.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the manifest-driven API of {@link SpecLoader}.
 *
 * <p>Uses a stub deserializer and in-memory manifest + spec files
 * written to a temp directory.
 *
 * <p>Ref: java-developer-skill.md §5.5, TASK-BE-016.
 */
class SpecLoaderManifestTests {

    // ── stub spec ─────────────────────────────────────────────────────────────

    /** Minimal NarrativeSpec stub used to test loading plumbing. */
    record StubSpec(String id, String blockId) implements NarrativeSpec {
        @Override public String getId()      { return id; }
        @Override public String getBlockId() { return blockId; }
    }

    /** Deserializer that produces one StubSpec regardless of XML content. */
    static class StubDeserializer implements SpecDeserializer<StubSpec> {
        final String id;
        StubDeserializer(String id) { this.id = id; }

        @Override
        public List<StubSpec> deserialize(InputStream stream, String sourceName) {
            return List.of(new StubSpec(id, "test-block"));
        }
    }

    // ── stub resolver ─────────────────────────────────────────────────────────

    private PathMatchingResourcePatternResolver resolverFor(Path... files) {
        return new PathMatchingResourcePatternResolver() {
            @Override public Resource getResource(String location) {
                // strip "classpath:" prefix and look in temp files
                String rel = location.replace("classpath:", "");
                for (Path f : files) {
                    if (f.toString().endsWith(rel.replace("/", FileSystems.getDefault().getSeparator())))
                        return new FileSystemResource(f);
                }
                // return a non-existing resource for unknown paths
                return new FileSystemResource(Path.of(rel));
            }
        };
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    void loadAll_manifest_loads_each_entry(@TempDir Path tmp) throws IOException {
        // prepare two fake spec files
        Path spec1 = Files.writeString(tmp.resolve("q1.xml"), "<quest/>", StandardCharsets.UTF_8);
        Path spec2 = Files.writeString(tmp.resolve("q2.xml"), "<quest/>", StandardCharsets.UTF_8);

        BlockManifest manifest = new BlockManifest("quest", "1.0", List.of(
                new SpecEntry("q1", "quest", "narrative/quest/q1/q1.xml"),
                new SpecEntry("q2", "quest", "narrative/quest/q2/q2.xml")
        ));

        // stub deserializer returns StubSpec with entity id from entry, just reuse same id
        SpecDeserializer<StubSpec> deser = (stream, name) ->
                List.of(new StubSpec(name.contains("q1") ? "q1" : "q2", "quest"));

        SpecLoader<StubSpec> loader = new SpecLoader<>(
                deser, resolverFor(spec1, spec2));

        List<StubSpec> result = loader.loadAll(manifest);
        assertThat(result).hasSize(2);
    }

    @Test
    void loadAll_manifest_missing_file_throws_SpecLoadException(@TempDir Path tmp) {
        BlockManifest manifest = new BlockManifest("quest", "1.0", List.of(
                new SpecEntry("missing", "quest", "narrative/quest/missing/spec.xml")
        ));
        SpecLoader<StubSpec> loader = new SpecLoader<>(
                new StubDeserializer("x"),
                new PathMatchingResourcePatternResolver());

        assertThatThrownBy(() -> loader.loadAll(manifest))
                .isInstanceOf(SpecLoadException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void loadAll_empty_manifest_returns_empty_list() {
        BlockManifest manifest = new BlockManifest("quest", "1.0", List.of());
        SpecLoader<StubSpec> loader = new SpecLoader<>(new StubDeserializer("x"));
        assertThat(loader.loadAll(manifest)).isEmpty();
    }
}
