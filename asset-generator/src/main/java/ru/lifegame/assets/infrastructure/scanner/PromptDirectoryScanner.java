package ru.lifegame.assets.infrastructure.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Scans asset-specs/ directory structure to discover ALL entity directories
 * containing visual-specs.xml. Recursively walks all subdirectories,
 * mirroring the full structure of asset-specs/ into the output.
 * <p>
 * Expected structure (all subdirectories are auto-discovered):
 * <pre>
 * asset-specs/
 *   characters/
 *     tanya/          ← entity (has visual-specs.xml)
 *     alexander/      ← entity
 *   furniture/
 *     bed/            ← entity
 *     computer/       ← entity
 *   locations/
 *     home_room/      ← entity
 *   system/
 *     ...
 *   ui/
 *     particles/      ← entity
 *     transitions/    ← entity
 *     notifications/  ← entity
 * </pre>
 * <p>
 * No hardcoded directory names — any new category added to asset-specs/
 * will be picked up automatically.
 */
public class PromptDirectoryScanner {

    private static final Logger log = LoggerFactory.getLogger(PromptDirectoryScanner.class);

    /** Name of the unified spec file to look for. */
    private static final String UNIFIED_SPEC_FILENAME = "visual-specs.xml";

    /**
     * Discovers all entity directories under the specs root by recursively
     * walking ALL subdirectories. An "entity directory" is any directory
     * that contains a visual-specs.xml file.
     *
     * @param specsRoot path to asset-specs/
     * @return list of entity directory paths containing visual-specs.xml
     */
    public List<Path> discoverEntities(Path specsRoot) {
        List<Path> entities = new ArrayList<>();
        if (!Files.isDirectory(specsRoot)) {
            log.warn("Specs root does not exist or is not a directory: {}", specsRoot);
            return entities;
        }
        scanRecursively(specsRoot, entities);
        log.info("Discovered {} entity directories under {}", entities.size(), specsRoot);
        return entities;
    }

    /**
     * Recursively scans a directory. If it contains visual-specs.xml,
     * it is treated as an entity directory. Otherwise, its subdirectories
     * are scanned further.
     */
    private void scanRecursively(Path dir, List<Path> result) {
        Path specFile = dir.resolve(UNIFIED_SPEC_FILENAME);
        if (Files.exists(specFile)) {
            result.add(dir);
            log.debug("Found entity: {}", dir);
            return; // Entity found — do not descend further
        }

        // Not an entity dir — scan children
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path child : stream) {
                if (Files.isDirectory(child)) {
                    scanRecursively(child, result);
                }
            }
        } catch (IOException e) {
            log.warn("Failed to scan directory: {}", dir, e);
        }
    }

    /**
     * Finds entity directories that are missing the unified XML spec file.
     * Scans ALL subdirectories (not just entities) to find dirs that look
     * like they should have specs but don't.
     *
     * @param specsRoot path to asset-specs/
     * @return list of leaf directories lacking visual-specs.xml
     */
    public List<Path> findMissingSpecs(Path specsRoot) {
        List<Path> missing = new ArrayList<>();
        if (!Files.isDirectory(specsRoot)) {
            return missing;
        }
        findLeafDirsWithoutSpec(specsRoot, missing);
        return missing;
    }

    /**
     * Finds leaf directories (directories with no subdirectories) that
     * are missing visual-specs.xml.
     */
    private void findLeafDirsWithoutSpec(Path dir, List<Path> result) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            boolean hasSubDirs = false;
            for (Path child : stream) {
                if (Files.isDirectory(child)) {
                    hasSubDirs = true;
                    findLeafDirsWithoutSpec(child, result);
                }
            }
            // Leaf directory without spec file
            if (!hasSubDirs && !Files.exists(dir.resolve(UNIFIED_SPEC_FILENAME))) {
                // Skip the root itself
                if (!dir.equals(dir.getParent())) {
                    result.add(dir);
                    log.debug("Missing unified spec: {}", dir);
                }
            }
        } catch (IOException e) {
            log.warn("Failed to scan directory: {}", dir, e);
        }
    }

    /**
     * Extracts the entity type from a full entity path.
     * Determines the type based on the directory immediately under specsRoot.
     * E.g. for "asset-specs/characters/tanya" returns "characters".
     * For deeper nesting like "asset-specs/ui/particles" returns "ui".
     *
     * @param entityDir the entity directory path
     * @param specsRoot the root specs directory for relative path calculation
     */
    public String extractEntityType(Path entityDir, Path specsRoot) {
        Path relative = specsRoot.relativize(entityDir);
        if (relative.getNameCount() > 0) {
            return relative.getName(0).toString();
        }
        return "unknown";
    }

    /**
     * Extracts the entity type from a full entity path (legacy single-arg version).
     * E.g. for "asset-specs/characters/tanya" returns "characters".
     */
    public String extractEntityType(Path entityDir) {
        Path parent = entityDir.getParent();
        return parent != null ? parent.getFileName().toString() : "unknown";
    }

    /**
     * Extracts the entity name from a full entity path.
     * E.g. for "asset-specs/characters/tanya" returns "tanya".
     */
    public String extractEntityName(Path entityDir) {
        return entityDir.getFileName().toString();
    }
}
