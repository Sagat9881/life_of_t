package ru.lifegame.assets.infrastructure.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.lifegame.assets.domain.model.asset.AssetSpec;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("XmlAssetSpecParser — парсинг XML спецификаций")
class XmlAssetSpecParserTest {

    private XmlAssetSpecParser parser;

    @BeforeEach
    void setUp() {
        parser = new XmlAssetSpecParser();
    }

    @Test
    @DisplayName("Парсинг location XML: 3 слоя, палитра, constraints")
    void parseLocationXml() {
        String xml = locationXml();
        AssetSpec spec = parser.parseFromStream(toStream(xml));

        assertThat(spec.entityType()).isEqualTo("locations");
        assertThat(spec.entityName()).isEqualTo("home");
        assertThat(spec.layers()).hasSize(3);
        assertThat(spec.layers().get(0).type()).isEqualTo("background");
        assertThat(spec.layers().get(1).type()).isEqualTo("midground");
        assertThat(spec.layers().get(2).type()).isEqualTo("foreground");
    }

    @Test
    @DisplayName("Парсинг character XML: слои + анимации")
    void parseCharacterXml() {
        String xml = characterXml();
        AssetSpec spec = parser.parseFromStream(toStream(xml));

        assertThat(spec.entityType()).isEqualTo("characters");
        assertThat(spec.entityName()).isEqualTo("tanya");
        assertThat(spec.layers()).hasSize(2);
        assertThat(spec.animations()).hasSize(2);
        assertThat(spec.animations().get(0).name()).isEqualTo("idle");
        assertThat(spec.animations().get(0).frames()).isEqualTo(24);
        assertThat(spec.animations().get(1).name()).isEqualTo("walk");
    }

    @Test
    @DisplayName("Парсинг pet XML: слои + анимации")
    void parsePetXml() {
        String xml = petXml();
        AssetSpec spec = parser.parseFromStream(toStream(xml));

        assertThat(spec.entityType()).isEqualTo("pets");
        assertThat(spec.entityName()).isEqualTo("garfield");
        assertThat(spec.layers()).hasSize(2);
    }

    @Test
    @DisplayName("Палитра по умолчанию при отсутствии color-palette")
    void defaultPaletteWhenMissing() {
        String xml = minimalXml();
        AssetSpec spec = parser.parseFromStream(toStream(xml));

        assertThat(spec.colorPalette().primary()).isNotEmpty();
        assertThat(spec.colorPalette().primary()).contains("#F5E6D3");
    }

    @Test
    @DisplayName("Constraints по умолчанию при отсутствии")
    void defaultConstraintsWhenMissing() {
        String xml = minimalXml();
        AssetSpec spec = parser.parseFromStream(toStream(xml));

        assertThat(spec.constraints().maxFileSizeKb()).isEqualTo(500);
        assertThat(spec.constraints().bitDepth()).isEqualTo(32);
    }

    @Test
    @DisplayName("Time-of-day вариации парсятся корректно")
    void parseTimeOfDayVariations() {
        String xml = locationXml();
        AssetSpec spec = parser.parseFromStream(toStream(xml));

        assertThat(spec.timeOfDayVariations()).hasSize(2);
        assertThat(spec.timeOfDayVariations().get(0).time()).isEqualTo("morning");
    }

    @Test
    @DisplayName("Отсутствие <layers> выбрасывает XmlParseException")
    void missingLayersThrows() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <asset-spec>
                    <meta>
                        <entity-type>locations</entity-type>
                        <entity-name>test</entity-name>
                    </meta>
                </asset-spec>
                """;
        assertThatThrownBy(() -> parser.parseFromStream(toStream(xml)))
                .isInstanceOf(XmlParseException.class);
    }

    @Test
    @DisplayName("Невалидный XML выбрасывает XmlParseException")
    void invalidXmlThrows() {
        String xml = "not xml at all";
        assertThatThrownBy(() -> parser.parseFromStream(toStream(xml)))
                .isInstanceOf(XmlParseException.class);
    }

    // --- XML fixtures ---

    private String locationXml() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <asset-spec version="1.0.0">
                    <meta>
                        <entity-type>locations</entity-type>
                        <entity-name>home</entity-name>
                        <version>1.0.0</version>
                    </meta>
                    <layers>
                        <layer id="background" type="background" z-order="0">Far wall with window</layer>
                        <layer id="midground" type="midground" z-order="1">Main room space</layer>
                        <layer id="foreground" type="foreground" z-order="2">Near elements</layer>
                    </layers>
                    <color-palette>
                        <primary>
                            <color hex="#FFF9E9"/>
                            <color hex="#FFB6C1"/>
                        </primary>
                        <secondary>
                            <color hex="#A8B2B7"/>
                        </secondary>
                    </color-palette>
                    <time-of-day-variations>
                        <variation time="morning">
                            <lighting>Soft golden light</lighting>
                            <mood>Calm awakening</mood>
                        </variation>
                        <variation time="evening">
                            <lighting>Warm artificial light</lighting>
                            <mood>Relaxation</mood>
                        </variation>
                    </time-of-day-variations>
                </asset-spec>
                """;
    }

    private String characterXml() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <asset-spec version="1.0.0">
                    <meta>
                        <entity-type>characters</entity-type>
                        <entity-name>tanya</entity-name>
                        <version>1.0.0</version>
                    </meta>
                    <layers>
                        <layer id="base" type="base" z-order="0">Base body</layer>
                        <layer id="outfit" type="outfit" z-order="1">Clothing</layer>
                    </layers>
                    <animations>
                        <animation name="idle" frames="24" fps="2" loop="true" frame-width="128" frame-height="128"/>
                        <animation name="walk" frames="24" fps="12" loop="true" frame-width="128" frame-height="128"/>
                    </animations>
                </asset-spec>
                """;
    }

    private String petXml() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <asset-spec version="1.0.0">
                    <meta>
                        <entity-type>pets</entity-type>
                        <entity-name>garfield</entity-name>
                        <version>1.0.0</version>
                    </meta>
                    <layers>
                        <layer id="base" type="base" z-order="0">Cat body</layer>
                        <layer id="markings" type="overlay" z-order="1">Orange tabby markings</layer>
                    </layers>
                    <animations>
                        <animation name="idle" frames="20" fps="6" loop="true" frame-width="64" frame-height="64"/>
                    </animations>
                </asset-spec>
                """;
    }

    private String minimalXml() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <asset-spec version="1.0.0">
                    <meta>
                        <entity-type>characters</entity-type>
                        <entity-name>minimal</entity-name>
                    </meta>
                    <layers>
                        <layer id="base" type="base">Minimal layer</layer>
                    </layers>
                </asset-spec>
                """;
    }

    private java.io.InputStream toStream(String xml) {
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }
}
