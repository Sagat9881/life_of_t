package ru.lifegame.assets.infrastructure.parser;

import ru.lifegame.assets.domain.model.asset.*;
import ru.lifegame.assets.infrastructure.scanner.EntityRef;
import ru.lifegame.assets.infrastructure.scanner.SpecsManifestReader;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link AssetSpecParser}.
 *
 * <p>Uses minimal valid asset spec XML written to in-memory streams
 * or temp files — no dependency on real asset XMLs.
 *
 * <p>Ref: java-developer-skill.md §3.2, §5.2. TASK-BE-017.
 */
class AssetSpecParserTests {

    // ── helpers ─────────────────────────────────────────────────────────────────

    /**
     * Builds minimal valid asset spec XML. XmlAssetSpecParser requires at minimum:
     * entity-type, entity-name, and at least one layer.
     */
    private String minimalXml(String entityType, String entityName, String extraBody) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <assetSpec>
                  <entityType>""".stripIndent() + entityType + """</entityType>
                  <entityName>""" + entityName + """</entityName>
                  <version>1.0</version>
                  <layers>
                    <layer id="base" type="base" z-order="0"
                           description="Base layer">
                    </layer>
                  </layers>
                """ + extraBody + """
                </assetSpec>
                """;
    }

    private InputStream stream(String xml) {
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }

    // stub XmlAssetSpecParser that returns a fixed AssetSpec
    private XmlAssetSpecParser stubXmlParser(AssetSpec fixed) {
        return new XmlAssetSpecParser() {
            @Override
            public AssetSpec parseDocument(org.w3c.dom.Document doc, String sourceName) {
                return fixed;
            }
        };
    }

    private AssetSpec baseSpec() {
        return new AssetSpec(
                "characters", "tanya", "1.0",
                List.of(new AssetLayer("base", "base", "Base", 0)),
                null, null, null, null
        );
    }

    // ── parse(Path) ───────────────────────────────────────────────────────────────

    @Test
    void parse_path_returns_spec_without_extensions(@TempDir Path tmp) throws IOException {
        AssetSpec base = baseSpec();
        Path file = Files.writeString(tmp.resolve("tanya.xml"),
                minimalXml("characters", "tanya", ""), StandardCharsets.UTF_8);

        AssetSpecParser parser = new AssetSpecParser(stubXmlParser(base));
        AssetSpec result = parser.parse(file);

        // Extensions absent — original spec returned unchanged
        assertThat(result.inheritsFrom()).isEmpty();
        assertThat(result.bindings()).isEmpty();
    }

    @Test
    void parse_path_missing_file_throws_XmlParseException(@TempDir Path tmp) {
        AssetSpecParser parser = new AssetSpecParser();
        assertThatThrownBy(() -> parser.parse(tmp.resolve("nonexistent.xml")))
                .isInstanceOf(XmlParseException.class);
    }

    // ── inheritsFrom ────────────────────────────────────────────────────────────

    @Test
    void parse_inheritsFrom_is_parsed() {
        AssetSpec base = baseSpec();
        String xml = minimalXml("characters", "tanya",
                "<inheritsFrom>characters/base_character</inheritsFrom>");

        AssetSpecParser parser = new AssetSpecParser(stubXmlParser(base));
        AssetSpec result = parser.parse(stream(xml), "test");

        assertThat(result.inheritsFrom())
                .isPresent()
                .hasValue(new AssetId("characters", "base_character"));
    }

    @Test
    void parse_inheritsFrom_absent_returns_empty() {
        AssetSpec base = baseSpec();
        String xml = minimalXml("characters", "tanya", "");

        AssetSpecParser parser = new AssetSpecParser(stubXmlParser(base));
        AssetSpec result = parser.parse(stream(xml), "test");

        assertThat(result.inheritsFrom()).isEmpty();
    }

    @Test
    void parse_inheritsFrom_invalid_format_throws() {
        AssetSpec base = baseSpec();
        String xml = minimalXml("characters", "tanya",
                "<inheritsFrom>bad_format_no_slash</inheritsFrom>");

        AssetSpecParser parser = new AssetSpecParser(stubXmlParser(base));
        assertThatThrownBy(() -> parser.parse(stream(xml), "test.xml"))
                .isInstanceOf(XmlParseException.class)
                .hasMessageContaining("inheritsFrom");
    }

    // ── bindings ─────────────────────────────────────────────────────────────────

    @Test
    void parse_bindings_with_params() {
        AssetSpec base = baseSpec();
        String xml = minimalXml("characters", "tanya", """
                <bindings>
                  <bind layer="eyes" behavior="blink">
                    <param name="interval_ms" value="3000"/>
                    <param name="duration_ms"  value="120"/>
                  </bind>
                  <bind layer="body" behavior="breathe"/>
                </bindings>
                """);

        AssetSpecParser parser = new AssetSpecParser(stubXmlParser(base));
        AssetSpec result = parser.parse(stream(xml), "test");

        assertThat(result.bindings()).hasSize(2);
        LayerBinding blink = result.bindings().get(0);
        assertThat(blink.layerId()).isEqualTo("eyes");
        assertThat(blink.behaviorId()).isEqualTo("blink");
        assertThat(blink.params()).containsEntry("interval_ms", "3000")
                                 .containsEntry("duration_ms",  "120");

        LayerBinding breathe = result.bindings().get(1);
        assertThat(breathe.layerId()).isEqualTo("body");
        assertThat(breathe.behaviorId()).isEqualTo("breathe");
        assertThat(breathe.params()).isEmpty();
    }

    @Test
    void parse_bindings_absent_returns_empty_list() {
        AssetSpec base = baseSpec();
        String xml = minimalXml("characters", "tanya", "");

        AssetSpecParser parser = new AssetSpecParser(stubXmlParser(base));
        AssetSpec result = parser.parse(stream(xml), "test");

        assertThat(result.bindings()).isEmpty();
    }

    @Test
    void parse_binding_missing_layer_attr_throws() {
        AssetSpec base = baseSpec();
        String xml = minimalXml("characters", "tanya", """
                <bindings>
                  <bind behavior="blink"/>
                </bindings>
                """);
        AssetSpecParser parser = new AssetSpecParser(stubXmlParser(base));
        assertThatThrownBy(() -> parser.parse(stream(xml), "test.xml"))
                .isInstanceOf(XmlParseException.class)
                .hasMessageContaining("layer");
    }

    // ── loadAll (manifest-driven) ────────────────────────────────────────────────

    @Test
    void loadAll_skips_abstract_entities() {
        // SpecsSource stub: manifest lists 1 concrete + 1 abstract entity
        String manifest = """
                <?xml version="1.0"?>
                <specs>
                  <entity path="characters/tanya.xml"/>
                  <entity path="characters/base.xml" abstract="true"/>
                </specs>""";
        SpecsSource source = new SpecsSource() {
            @Override public boolean specExists(String p) { return true; }
            @Override public InputStream openSpec(String p) {
                if (p.equals("specs-manifest.xml"))
                    return new ByteArrayInputStream(manifest.getBytes(StandardCharsets.UTF_8));
                // non-abstract entity — return minimal spec stream (parser will be stubbed anyway)
                return new ByteArrayInputStream(
                        minimalXml("characters", "tanya", "").getBytes(StandardCharsets.UTF_8));
            }
        };

        AssetSpec tanya = baseSpec();
        AssetSpecParser parser = new AssetSpecParser(stubXmlParser(tanya));
        List<AssetSpec> result = parser.loadAll(source);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).entityName()).isEqualTo("tanya");
    }

    // ── AssetId.parse ─────────────────────────────────────────────────────────────────

    @Test
    void assetId_parse_valid() {
        AssetId id = AssetId.parse("characters/tanya");
        assertThat(id.entityType()).isEqualTo("characters");
        assertThat(id.entityName()).isEqualTo("tanya");
        assertThat(id.toString()).isEqualTo("characters/tanya");
    }

    @Test
    void assetId_parse_invalid_throws() {
        assertThatThrownBy(() -> AssetId.parse("no_slash_here"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AssetId.parse("trailing/"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── LayerBinding ─────────────────────────────────────────────────────────────────

    @Test
    void layerBinding_no_params_constructor() {
        LayerBinding b = new LayerBinding("eyes", "blink");
        assertThat(b.params()).isEmpty();
        assertThat(b.layerId()).isEqualTo("eyes");
    }

    @Test
    void layerBinding_null_params_becomes_empty_map() {
        LayerBinding b = new LayerBinding("eyes", "blink", null);
        assertThat(b.params()).isEmpty();
    }
}
