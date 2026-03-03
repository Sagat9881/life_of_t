package ru.lifegame.assets.application.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.lifegame.assets.domain.model.asset.AssetSpec;
import ru.lifegame.assets.infrastructure.parser.XmlAssetSpecParser;
import ru.lifegame.assets.infrastructure.scanner.PromptDirectoryScanner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link UnifyXmlSpecsUseCase}.
 *
 * <p>Uses real {@link PromptDirectoryScanner} and {@link XmlAssetSpecParser}
 * implementations with an in-memory temp directory, so no mocking is needed.</p>
 */
class UnifyXmlSpecsTest {

    @TempDir
    Path tempDir;

    private final UnifyXmlSpecsUseCase useCase = new UnifyXmlSpecsUseCase(
            new PromptDirectoryScanner(),
            new XmlAssetSpecParser()
    );

    // -----------------------------------------------------------------------
    // execute()
    // -----------------------------------------------------------------------

    @Test
    void execute_twoSpecFiles_returnsTwoParsedSpecs() throws IOException {
        createSpec("characters", "tanya",
                "<asset id='tanya' category='character'/>");
        createSpec("locations", "home",
                "<asset id='home' category='location'/>");

        List<AssetSpec> specs = useCase.execute(tempDir);

        assertThat(specs).hasSize(2);
        assertThat(specs).anyMatch(s -> s.id().equals("tanya"));
        assertThat(specs).anyMatch(s -> s.id().equals("home"));
    }

    @Test
    void execute_emptyRoot_returnsEmptyList() {
        List<AssetSpec> specs = useCase.execute(tempDir);
        assertThat(specs).isEmpty();
    }

    @Test
    void execute_singleSpec_returnsOneItem() throws IOException {
        createSpec("pets", "garfield",
                "<asset id='garfield' category='pet'/>");

        List<AssetSpec> specs = useCase.execute(tempDir);

        assertThat(specs).hasSize(1);
        assertThat(specs.get(0).id()).isEqualTo("garfield");
        assertThat(specs.get(0).category()).isEqualTo("pet");
    }

    @Test
    void execute_specWithLayers_parsesLayersCorrectly() throws IOException {
        createSpec("characters", "hero", """
                <asset id='hero' category='character'>
                  <layers>
                    <layer name='base' type='base' zIndex='0' optional='false'/>
                    <layer name='hat'  type='overlay' zIndex='1' optional='true'/>
                  </layers>
                </asset>
                """);

        List<AssetSpec> specs = useCase.execute(tempDir);

        assertThat(specs).hasSize(1);
        AssetSpec hero = specs.get(0);
        assertThat(hero.layers()).hasSize(2);
        assertThat(hero.layers().get(1).optional()).isTrue();
    }

    @Test
    void execute_multipleCategories_collectsAllSpecs() throws IOException {
        createSpec("characters", "tanya",   "<asset id='tanya'   category='character'/>");
        createSpec("characters", "husband", "<asset id='husband' category='character'/>");
        createSpec("locations",  "home",    "<asset id='home'    category='location'/>");
        createSpec("pets",       "garfield", "<asset id='garfield' category='pet'/>");

        List<AssetSpec> specs = useCase.execute(tempDir);

        assertThat(specs).hasSize(4);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void createSpec(String category, String entity, String xml) throws IOException {
        Path dir = Files.createDirectories(tempDir.resolve(category).resolve(entity));
        Files.writeString(dir.resolve("visual-specs.xml"), xml, StandardCharsets.UTF_8);
    }
}
