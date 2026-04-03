package ru.lifegame.assets.application.usecase;

import org.junit.jupiter.api.Test;
import ru.lifegame.assets.domain.model.docs.DocsPreviewResult;
import ru.lifegame.assets.domain.model.docs.EntityDocsDescriptor;
import ru.lifegame.assets.infrastructure.docs.DocsPreviewXmlParser;
import ru.lifegame.assets.infrastructure.parser.SpecsSource;
import ru.lifegame.assets.infrastructure.parser.XmlParseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link DocsPreviewUseCase}.
 * All dependencies are hand-crafted stubs — no Spring context, no file I/O.
 *
 * <p>Covers:
 * <ul>
 *   <li>Happy path: 2 concrete entities + 1 abstract → 2 descriptors returned.</li>
 *   <li>Missing XML spec: entity listed in manifest but spec file absent → skipped with warning.</li>
 *   <li>Empty manifest: 0 descriptors returned.</li>
 * </ul>
 */
class DocsPreviewUseCaseTest {

    // ── minimal visual-specs.xml for tests ───────────────────────────────

    private static final String MANIFEST_CONTENT = """
            <?xml version="1.0" encoding="UTF-8"?>
            <manifest>
              <entity path="characters/hero" abstract="false"/>
              <entity path="locations/town"  abstract="false"/>
              <entity path="abstract/base"   abstract="true"/>
            </manifest>
            """;

    private static final String HERO_SPEC = """
            <?xml version="1.0" encoding="UTF-8"?>
            <spec>
              <meta>
                <entity-type>characters</entity-type>
                <entity-name>hero</entity-name>
                <version>1.0</version>
              </meta>
              <animations>
                <animation name="idle" frames="8" fps="8" loop="true"/>
                <animation name="walk" frames="4" fps="8" loop="true"/>
              </animations>
              <color-palette>
                <color id="skin" value="#F0C098"/>
              </color-palette>
              <layers>
                <layer id="body" type="body" z-order="0" width="64" height="64"/>
              </layers>
            </spec>
            """;

    private static final String TOWN_SPEC = """
            <?xml version="1.0" encoding="UTF-8"?>
            <spec>
              <meta>
                <entity-type>locations</entity-type>
                <entity-name>town</entity-name>
                <version>1.0</version>
              </meta>
              <animations>
                <animation name="day" frames="1" fps="1" loop="false"/>
              </animations>
              <layers>
                <layer id="bg" type="background" z-order="0" width="320" height="240"/>
              </layers>
            </spec>
            """;

    // ── stub SpecsSource ─────────────────────────────────────────────────

    private static SpecsSource stubSource(java.util.Map<String, String> files) {
        return new SpecsSource() {
            @Override
            public boolean specExists(String path) {
                return files.containsKey(path);
            }

            @Override
            public InputStream openSpec(String path) throws IOException {
                String content = files.get(path);
                if (content == null) throw new IOException("not found: " + path);
                return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            }
        };
    }

    // ── tests ────────────────────────────────────────────────────────────

    /**
     * Happy path: manifest has 2 concrete + 1 abstract.
     * Expected: 2 descriptors, no abstract entities in result.
     */
    @Test
    void execute_happyPath_returnsTwoDescriptors() {
        var files = java.util.Map.of(
                "specs-manifest.xml",              MANIFEST_CONTENT,
                "characters/hero/visual-specs.xml", HERO_SPEC,
                "locations/town/visual-specs.xml",  TOWN_SPEC
        );
        SpecsSource source = stubSource(files);
        DocsPreviewUseCase useCase = new DocsPreviewUseCase(source, new DocsPreviewXmlParser());

        DocsPreviewResult result = useCase.execute();

        assertThat(result.descriptors()).hasSize(2);

        List<String> ids = result.descriptors().stream()
                .map(EntityDocsDescriptor::id)
                .toList();
        assertThat(ids).containsExactly("hero", "town");
    }

    /**
     * Animations parsed correctly from hero spec.
     */
    @Test
    void execute_heroDescriptor_containsAnimations() {
        var files = java.util.Map.of(
                "specs-manifest.xml",              MANIFEST_CONTENT,
                "characters/hero/visual-specs.xml", HERO_SPEC,
                "locations/town/visual-specs.xml",  TOWN_SPEC
        );
        DocsPreviewUseCase useCase = new DocsPreviewUseCase(stubSource(files), new DocsPreviewXmlParser());

        DocsPreviewResult result = useCase.execute();

        EntityDocsDescriptor hero = result.descriptors().get(0);
        assertThat(hero.id()).isEqualTo("hero");
        assertThat(hero.type()).isEqualTo("characters");
        assertThat(hero.displayName()).isEqualTo("Hero");
        assertThat(hero.animations()).containsExactly("idle", "walk");
        assertThat(hero.spriteAtlasFile()).isEqualTo("hero_idle.png");
        assertThat(hero.colorPalette()).hasSize(1);
        assertThat(hero.colorPalette().get(0).hex()).isEqualTo("#F0C098");
    }

    /**
     * Missing XML spec: entity listed in manifest but spec file absent → skipped.
     */
    @Test
    void execute_missingSpec_entitySkipped() {
        var files = java.util.Map.of(
                "specs-manifest.xml",              MANIFEST_CONTENT,
                "characters/hero/visual-specs.xml", HERO_SPEC
                // town spec intentionally absent
        );
        DocsPreviewUseCase useCase = new DocsPreviewUseCase(stubSource(files), new DocsPreviewXmlParser());

        DocsPreviewResult result = useCase.execute();

        // Only hero is returned; town is silently skipped
        assertThat(result.descriptors()).hasSize(1);
        assertThat(result.descriptors().get(0).id()).isEqualTo("hero");
    }

    /**
     * Empty manifest: 0 descriptors returned.
     */
    @Test
    void execute_emptyManifest_returnsEmpty() {
        String emptyManifest = """  
                <?xml version="1.0" encoding="UTF-8"?>
                <manifest/>
                """;
        var files = java.util.Map.of("specs-manifest.xml", emptyManifest);
        DocsPreviewUseCase useCase = new DocsPreviewUseCase(stubSource(files), new DocsPreviewXmlParser());

        DocsPreviewResult result = useCase.execute();

        assertThat(result.descriptors()).isEmpty();
    }

    /**
     * Missing manifest: XmlParseException is thrown.
     */
    @Test
    void execute_missingManifest_throwsException() {
        SpecsSource emptySource = stubSource(java.util.Map.of());
        DocsPreviewUseCase useCase = new DocsPreviewUseCase(emptySource, new DocsPreviewXmlParser());

        assertThatThrownBy(useCase::execute)
                .isInstanceOf(XmlParseException.class)
                .hasMessageContaining("specs-manifest.xml");
    }
}
