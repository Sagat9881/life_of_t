package ru.lifegame.assets.infrastructure.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.lifegame.assets.domain.model.asset.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link XmlAssetSpecParser}.
 *
 * <p>Each test writes a minimal (or intentionally broken) XML file to a
 * {@link TempDir} and then calls {@code parse()} on it.  This avoids any
 * classpath-resource dependency and keeps the tests hermetically isolated.</p>
 */
class XmlAssetSpecParserTest {

    @TempDir
    Path tempDir;

    private final XmlAssetSpecParser parser = new XmlAssetSpecParser();

    // -----------------------------------------------------------------------
    // Happy-path: fully populated spec
    // -----------------------------------------------------------------------

    @Test
    void parse_fullSpec_returnsPopulatedAssetSpec() throws IOException {
        Path file = writeXml(tempDir, "full.xml", """
                <?xml version="1.0" encoding="UTF-8"?>
                <asset id="tanya_idle" category="character">
                  <description>Tanya idle sprite</description>
                  <layers>
                    <layer name="base" type="base" paletteRef="skin" zIndex="0" optional="false"/>
                    <layer name="eyes" type="overlay" paletteRef="eye_color" zIndex="1" optional="true"/>
                  </layers>
                  <animations>
                    <animation state="idle" frameCount="4" fps="8" loop="true">
                      <frames>0 1 2 3</frames>
                    </animation>
                  </animations>
                  <palettes>
                    <palette name="skin">
                      <color>#FFDFC4</color>
                      <color>#F0C080</color>
                    </palette>
                  </palettes>
                  <variations>
                    <variation timeOfDay="evening" colorShift="#FF8800" opacity="0.2"/>
                  </variations>
                  <naming prefix="tanya"
                          layerPattern="%s_layer_%02d.png"
                          atlasPattern="%s_atlas.webp"
                          configPattern="%s_atlas.json"/>
                  <constraints widthPx="64" heightPx="64" maxFrames="16"
                               allowTransparency="true" outputFormats="png,webp"/>
                </asset>
                """);

        AssetSpec spec = parser.parse(file);

        assertThat(spec.id()).isEqualTo("tanya_idle");
        assertThat(spec.category()).isEqualTo("character");
        assertThat(spec.description()).isEqualTo("Tanya idle sprite");

        // Layers
        assertThat(spec.layers()).hasSize(2);
        assertThat(spec.layers().get(0).name()).isEqualTo("base");
        assertThat(spec.layers().get(1).optional()).isTrue();

        // Animations
        assertThat(spec.animations()).hasSize(1);
        assertThat(spec.animations().get(0).state()).isEqualTo("idle");
        assertThat(spec.animations().get(0).frames()).containsExactly(0, 1, 2, 3);

        // Palettes
        assertThat(spec.palettes()).hasSize(1);
        assertThat(spec.palettes().get(0).name()).isEqualTo("skin");
        assertThat(spec.palettes().get(0).colors()).containsExactly("#FFDFC4", "#F0C080");

        // Variations
        assertThat(spec.variations()).hasSize(1);
        assertThat(spec.variations().get(0).timeOfDay()).isEqualTo("evening");
        assertThat(spec.variations().get(0).opacity()).isEqualTo(0.2);

        // Naming
        assertThat(spec.naming().prefix()).isEqualTo("tanya");

        // Constraints
        assertThat(spec.constraints().widthPx()).isEqualTo(64);
        assertThat(spec.constraints().outputFormats()).isEqualTo("png,webp");
    }

    // -----------------------------------------------------------------------
    // Minimal spec (only mandatory fields)
    // -----------------------------------------------------------------------

    @Test
    void parse_minimalSpec_returnsSpecWithEmptyCollections() throws IOException {
        Path file = writeXml(tempDir, "minimal.xml", """
                <?xml version="1.0" encoding="UTF-8"?>
                <asset id="hero" category="character">
                </asset>
                """);

        AssetSpec spec = parser.parse(file);

        assertThat(spec.id()).isEqualTo("hero");
        assertThat(spec.layers()).isEmpty();
        assertThat(spec.animations()).isEmpty();
        assertThat(spec.palettes()).isEmpty();
        assertThat(spec.variations()).isEmpty();
        assertThat(spec.naming()).isNull();
        assertThat(spec.constraints()).isNull();
    }

    // -----------------------------------------------------------------------
    // Missing mandatory attribute
    // -----------------------------------------------------------------------

    @Test
    void parse_missingId_throwsXmlParseException() throws IOException {
        Path file = writeXml(tempDir, "no_id.xml", """
                <?xml version="1.0" encoding="UTF-8"?>
                <asset category="character">
                </asset>
                """);

        assertThatThrownBy(() -> parser.parse(file))
                .isInstanceOf(XmlParseException.class)
                .hasMessageContaining("id");
    }

    @Test
    void parse_missingCategory_throwsXmlParseException() throws IOException {
        Path file = writeXml(tempDir, "no_cat.xml", """
                <?xml version="1.0" encoding="UTF-8"?>
                <asset id="hero">
                </asset>
                """);

        assertThatThrownBy(() -> parser.parse(file))
                .isInstanceOf(XmlParseException.class)
                .hasMessageContaining("category");
    }

    // -----------------------------------------------------------------------
    // Wrong root element
    // -----------------------------------------------------------------------

    @Test
    void parse_wrongRootElement_throwsXmlParseException() throws IOException {
        Path file = writeXml(tempDir, "wrong_root.xml", """
                <?xml version="1.0" encoding="UTF-8"?>
                <spec id="x" category="y"></spec>
                """);

        assertThatThrownBy(() -> parser.parse(file))
                .isInstanceOf(XmlParseException.class)
                .hasMessageContaining("asset");
    }

    // -----------------------------------------------------------------------
    // Layer defaults
    // -----------------------------------------------------------------------

    @Test
    void parse_layerDefaults_zIndexZeroAndNotOptional() throws IOException {
        Path file = writeXml(tempDir, "layer_defaults.xml", """
                <?xml version="1.0" encoding="UTF-8"?>
                <asset id="hero" category="character">
                  <layers>
                    <layer name="base" type="base"/>
                  </layers>
                </asset>
                """);

        AssetSpec spec = parser.parse(file);
        assertThat(spec.layers().get(0).zIndex()).isEqualTo(0);
        assertThat(spec.layers().get(0).optional()).isFalse();
    }

    // -----------------------------------------------------------------------
    // Animation with no <frames>
    // -----------------------------------------------------------------------

    @Test
    void parse_animationNoFrames_returnsEmptyFrameList() throws IOException {
        Path file = writeXml(tempDir, "no_frames.xml", """
                <?xml version="1.0" encoding="UTF-8"?>
                <asset id="hero" category="character">
                  <animations>
                    <animation state="idle" frameCount="4" fps="8" loop="true"/>
                  </animations>
                </asset>
                """);

        AssetSpec spec = parser.parse(file);
        assertThat(spec.animations().get(0).frames()).isEmpty();
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private static Path writeXml(Path dir, String name, String content) throws IOException {
        Path file = dir.resolve(name);
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }
}
