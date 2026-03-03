package ru.lifegame.assets.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.lifegame.assets.domain.model.asset.AssetSpec;
import ru.lifegame.assets.infrastructure.parser.XmlAssetSpecParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UnifyXmlSpecsUseCase — приведение к unified format")
class UnifyXmlSpecsTest {

    private UnifyXmlSpecsUseCase useCase;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        useCase = new UnifyXmlSpecsUseCase(new XmlAssetSpecParser());
    }

    @Test
    @DisplayName("Конвертация character XML в AssetSpec")
    void convertCharacterXml() throws IOException {
        Path xmlFile = tempDir.resolve("visual-specs.xml");
        Files.writeString(xmlFile, characterXml(), StandardCharsets.UTF_8);

        AssetSpec spec = useCase.execute(xmlFile);

        assertThat(spec.entityType()).isEqualTo("characters");
        assertThat(spec.entityName()).isEqualTo("tanya");
        assertThat(spec.layers()).isNotEmpty();
        assertThat(spec.colorPalette()).isNotNull();
        assertThat(spec.constraints()).isNotNull();
    }

    @Test
    @DisplayName("Конвертация location XML в AssetSpec")
    void convertLocationXml() throws IOException {
        Path xmlFile = tempDir.resolve("visual-specs.xml");
        Files.writeString(xmlFile, locationXml(), StandardCharsets.UTF_8);

        AssetSpec spec = useCase.execute(xmlFile);

        assertThat(spec.entityType()).isEqualTo("locations");
        assertThat(spec.entityName()).isEqualTo("home");
        assertThat(spec.layers()).hasSize(3);
    }

    @Test
    @DisplayName("Unified spec содержит все обязательные поля")
    void unifiedSpecContainsAllFields() throws IOException {
        Path xmlFile = tempDir.resolve("visual-specs.xml");
        Files.writeString(xmlFile, characterXml(), StandardCharsets.UTF_8);

        AssetSpec spec = useCase.execute(xmlFile);

        assertThat(spec.entityType()).isNotBlank();
        assertThat(spec.entityName()).isNotBlank();
        assertThat(spec.version()).isNotBlank();
        assertThat(spec.layers()).isNotEmpty();
        assertThat(spec.colorPalette()).isNotNull();
        assertThat(spec.naming()).isNotNull();
        assertThat(spec.constraints()).isNotNull();
    }

    @Test
    @DisplayName("Анимации из XML переносятся в AssetSpec")
    void animationsPreserved() throws IOException {
        Path xmlFile = tempDir.resolve("visual-specs.xml");
        Files.writeString(xmlFile, characterXml(), StandardCharsets.UTF_8);

        AssetSpec spec = useCase.execute(xmlFile);

        assertThat(spec.animations()).hasSize(1);
        assertThat(spec.animations().get(0).name()).isEqualTo("idle");
        assertThat(spec.animations().get(0).frames()).isEqualTo(24);
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
                    </animations>
                </asset-spec>
                """;
    }

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
                        <layer id="background" type="background" z-order="0">Wall</layer>
                        <layer id="midground" type="midground" z-order="1">Furniture</layer>
                        <layer id="foreground" type="foreground" z-order="2">Front frame</layer>
                    </layers>
                </asset-spec>
                """;
    }
}
