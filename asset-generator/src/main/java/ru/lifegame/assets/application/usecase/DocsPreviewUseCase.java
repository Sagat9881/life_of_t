package ru.lifegame.assets.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.assets.domain.model.docs.*;
import ru.lifegame.assets.infrastructure.parser.SpecsSource;
import ru.lifegame.assets.infrastructure.parser.XmlParseException;
import ru.lifegame.assets.infrastructure.scanner.EntityRef;
import ru.lifegame.assets.infrastructure.scanner.SpecsManifestReader;
import ru.lifegame.assets.infrastructure.docs.DocsPreviewXmlParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Application use-case: generates a {@link DocsPreviewResult} by reading the
 * specs manifest and parsing every non-abstract entity's XML spec.
 *
 * <p>Design constraints:
 * <ul>
 *   <li>No hardcoded entity IDs — the entity list is derived exclusively from
 *       {@code specs-manifest.xml} (java-developer-skill.md §5.2, §5.5).</li>
 *   <li>No «switch/if» on entity names or types.</li>
 *   <li>Pure domain/application logic — no JSON serialisation or file I/O here.</li>
 * </ul>
 *
 * <p>Conforms to ADR-001: data independence — the set of entities is configuration,
 * not code.
 *
 * <p>Ref: TASK-BE-DOC-001, visual-docs-preview-mode.md.
 */
public class DocsPreviewUseCase {

    private static final Logger log = LoggerFactory.getLogger(DocsPreviewUseCase.class);

    private final SpecsSource source;
    private final DocsPreviewXmlParser xmlParser;

    /**
     * Production constructor.
     *
     * @param source    abstraction for accessing spec files (classpath or disk)
     * @param xmlParser lightweight docs-focused XML parser
     */
    public DocsPreviewUseCase(SpecsSource source, DocsPreviewXmlParser xmlParser) {
        this.source    = Objects.requireNonNull(source,    "source must not be null");
        this.xmlParser = Objects.requireNonNull(xmlParser, "xmlParser must not be null");
    }

    /**
     * Executes the use case: reads the manifest, parses each non-abstract entity
     * and returns an ordered {@link DocsPreviewResult}.
     *
     * @return result with N descriptors where N = count of abstract=false in manifest
     * @throws XmlParseException if the manifest is missing or any XML spec is malformed
     */
    public DocsPreviewResult execute() {
        SpecsManifestReader manifestReader = new SpecsManifestReader(source);
        List<EntityRef> allRefs = manifestReader.readManifest();

        List<EntityDocsDescriptor> descriptors = new ArrayList<>();
        int skipped = 0;

        for (EntityRef ref : allRefs) {
            if (ref.isAbstract()) {
                log.debug("docs-preview: skipping abstract entity: {}", ref.path());
                skipped++;
                continue;
            }

            EntityDocsDescriptor descriptor = parseEntity(ref);
            if (descriptor != null) {
                descriptors.add(descriptor);
                log.debug("docs-preview: parsed entity: {}", ref.path());
            }
        }

        log.info("DocsPreviewUseCase: built {} descriptors, skipped {} abstract",
                descriptors.size(), skipped);
        return new DocsPreviewResult(descriptors);
    }

    /**
     * Parses a single entity from its XML spec. Returns {@code null} and logs
     * a warning if the spec file does not exist (manifest inconsistency — tolerated).
     */
    private EntityDocsDescriptor parseEntity(EntityRef ref) {
        // Convention: spec file is at <path>/visual-specs.xml
        String specPath = ref.path() + "/visual-specs.xml";

        if (!source.specExists(specPath)) {
            log.warn("docs-preview: no visual-specs.xml for entity '{}' — skipping", ref.path());
            return null;
        }

        try (InputStream is = source.openSpec(specPath)) {
            return xmlParser.parse(is, ref.path());
        } catch (XmlParseException e) {
            throw e;
        } catch (IOException e) {
            throw new XmlParseException(
                    "docs-preview: cannot open spec '" + specPath + "'", e);
        }
    }
}
