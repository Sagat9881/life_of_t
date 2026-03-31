package ru.lifegame.backend.infrastructure.spec;

import ru.lifegame.backend.domain.narrative.spec.BlockManifest;
import ru.lifegame.backend.domain.narrative.spec.SpecEntry;
import ru.lifegame.backend.domain.narrative.spec.SpecLoadException;

import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link XmlManifestParser}.
 *
 * <p>All tests use in-memory XML — no file I/O.
 * Ref: java-developer-skill.md §7, TASK-BE-016.
 */
class XmlManifestParserTests {

    private final XmlManifestParser parser = new XmlManifestParser();

    private InputStream xml(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Nested
    class ValidManifests {

        @Test
        void parses_block_id_and_version() {
            String src = """
                    <?xml version="1.0"?>
                    <manifest block-id="quest" version="2.0">
                    </manifest>""";
            BlockManifest m = parser.parse(xml(src), "test");
            assertThat(m.blockId()).isEqualTo("quest");
            assertThat(m.version()).isEqualTo("2.0");
        }

        @Test
        void missing_version_defaults_to_1_0() {
            String src = """
                    <?xml version="1.0"?>
                    <manifest block-id="npc">
                    </manifest>""";
            BlockManifest m = parser.parse(xml(src), "test");
            assertThat(m.version()).isEqualTo("1.0");
        }

        @Test
        void parses_entries_correctly() {
            String src = """
                    <?xml version="1.0"?>
                    <manifest block-id="npc" version="1.0">
                      <entry entity-id="aijan" entity-type="npc"
                             spec-path="narrative/npc/aijan.xml"/>
                      <entry entity-id="mama"  entity-type="npc"
                             spec-path="narrative/npc/mama.xml"/>
                    </manifest>""";
            BlockManifest m = parser.parse(xml(src), "test");
            assertThat(m.entries()).hasSize(2);
            SpecEntry first = m.entries().get(0);
            assertThat(first.entityId()).isEqualTo("aijan");
            assertThat(first.entityType()).isEqualTo("npc");
            assertThat(first.specPath()).isEqualTo("narrative/npc/aijan.xml");
        }

        @Test
        void empty_manifest_has_no_entries() {
            String src = """
                    <?xml version="1.0"?>
                    <manifest block-id="world-events" version="1.0">
                    </manifest>""";
            BlockManifest m = parser.parse(xml(src), "test");
            assertThat(m.isEmpty()).isTrue();
            assertThat(m.size()).isZero();
        }

        @Test
        void entriesByType_filters_correctly() {
            String src = """
                    <?xml version="1.0"?>
                    <manifest block-id="mixed" version="1.0">
                      <entry entity-id="q1" entity-type="quest"
                             spec-path="narrative/quest/q1/spec.xml"/>
                      <entry entity-id="n1" entity-type="npc"
                             spec-path="narrative/npc/n1.xml"/>
                    </manifest>""";
            BlockManifest m = parser.parse(xml(src), "test");
            assertThat(m.entriesByType("quest")).hasSize(1)
                    .extracting(SpecEntry::entityId).containsExactly("q1");
            assertThat(m.entriesByType("npc")).hasSize(1)
                    .extracting(SpecEntry::entityId).containsExactly("n1");
            assertThat(m.entriesByType("unknown")).isEmpty();
        }
    }

    @Nested
    class InvalidManifests {

        @Test
        void malformed_xml_throws_SpecLoadException() {
            assertThatThrownBy(() -> parser.parse(xml("NOT XML"), "bad.xml"))
                    .isInstanceOf(SpecLoadException.class)
                    .satisfies(ex -> assertThat(
                            ((SpecLoadException) ex).getSourceName()).isEqualTo("bad.xml"));
        }

        @Test
        void missing_block_id_throws_SpecLoadException() {
            String src = """
                    <?xml version="1.0"?>
                    <manifest version="1.0">
                    </manifest>""";
            assertThatThrownBy(() -> parser.parse(xml(src), "no-block-id.xml"))
                    .isInstanceOf(SpecLoadException.class)
                    .hasMessageContaining("block-id");
        }

        @Test
        void entry_missing_entity_id_throws_SpecLoadException() {
            String src = """
                    <?xml version="1.0"?>
                    <manifest block-id="quest" version="1.0">
                      <entry entity-type="quest" spec-path="narrative/quest/q1/spec.xml"/>
                    </manifest>""";
            assertThatThrownBy(() -> parser.parse(xml(src), "bad-entry.xml"))
                    .isInstanceOf(SpecLoadException.class)
                    .hasMessageContaining("entity-id");
        }
    }
}
