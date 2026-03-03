package ru.lifegame.assets.infrastructure.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Scans docs/prompts/ directory structure to discover entities and
 * identify those missing unified XML specifications.
 * <p>
 * Expected structure:
 * <pre>
 * docs/prompts/
 *   characters/
 *     tanya/        ← entity directory
 *     husband/      ← entity directory
 *   locations/
 *     home/         ← entity directory
 *   pets/
 *     garfield/     ← entity directory
 * </pre>
 */
public class PromptDirectoryScanner {

    private static final Logger log = LoggerFactory.getLogger(PromptDirectoryScanner.class);

    /** Entity type directories to scan. */
    private static final Set<String> ENTITY_TYPE_DIRS = Set.of(
            "characters", "locations", "pets"
    );

    /** Name of the unified spec file to look for. */
    private static final String UNIFIED_SPEC_FILENAME = "visual-specs.xml";

    /**
     * Discovers all entity directories under the prompts root.
     *
     * @param promptsRoot path to docs/prompts/
     * @return list of entity directory paths (e.g. docs/prompts/characters/tanya)
     */
    public List<Path> discoverEntities(Path promptsRoot) {
        List<Path> entities = new ArrayList<>();
        for (String typeDir : ENTITY_TYPE_DIRS) {
            Path typePath = promptsRoot.resolve(typeDir);
            if (!Files.isDirectory(typePath)) {
                continue;
            }
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(typePath)) {
                for (Path entry : stream) {
                    if (Files.isDirectory(entry)) {
                        entities.add(entry);
                    }
                }
            } catch (IOException e) {
                log.warn("Failed to scan directory: {}", typePath, e);
            }
        }
        return entities;
    }

    /**
     * Finds entity directories that are missing the unified XML spec file.
     *
     * @param promptsRoot path to docs/prompts/
     * @return list of entity directories lacking visual-specs.xml
     */
    public List<Path> findMissingSpecs(Path promptsRoot) {
        List<Path> allEntities = discoverEntities(promptsRoot);
        List<Path> missing = new ArrayList<>();
        for (Path entity : allEntities) {
            Path specFile = entity.resolve(UNIFIED_SPEC_FILENAME);
            if (!Files.exists(specFile)) {
                missing.add(entity);
                log.debug("Missing unified spec: {}", entity);
            }
        }
        return missing;
    }

    /**
     * Extracts the entity type from a full entity path.
     * E.g. for "docs/prompts/characters/tanya" returns "characters".
     */
    public String extractEntityType(Path entityDir) {
        Path parent = entityDir.getParent();
        return parent != null ? parent.getFileName().toString() : "unknown";
    }

    /**
     * Extracts the entity name from a full entity path.
     * E.g. for "docs/prompts/characters/tanya" returns "tanya".
     */
    public String extractEntityName(Path entityDir) {
        return entityDir.getFileName().toString();
    }
}
