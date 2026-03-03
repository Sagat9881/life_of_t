package ru.lifegame.assets.infrastructure.scanner;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Scans a {@code docs/prompts/} directory tree and discovers entity
 * sub-directories that contain (or are missing) a {@code visual-specs.xml}
 * file.
 *
 * <h2>Directory convention</h2>
 * <pre>
 * docs/prompts/
 *   _core/                 ← skipped (starts with '_')
 *   characters/
 *     tanya/
 *       visual-specs.xml   ← "has spec"
 *     husband/             ← "missing spec" (no XML file)
 *   locations/
 *     home/
 *       visual-specs.xml
 * </pre>
 *
 * <p>Only <em>leaf</em> directories (i.e., directories that contain no
 * sub-directories) are considered entity directories.  Category directories
 * (e.g., {@code characters/}, {@code locations/}) are transparent.</p>
 */
@Component
public class PromptDirectoryScanner {

    private static final String SPEC_FILENAME = "visual-specs.xml";

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Return all entity directories beneath {@code promptsRoot} that are
     * missing a {@code visual-specs.xml} file.
     *
     * @param promptsRoot root of the prompts directory tree
     * @return unmodifiable list of directories without a spec file
     */
    public List<Path> findMissingSpecs(Path promptsRoot) {
        return leafDirectories(promptsRoot)
                .filter(dir -> !hasSpecFile(dir))
                .toList();
    }

    /**
     * Return the paths to all {@code visual-specs.xml} files found anywhere
     * under {@code promptsRoot}.
     *
     * @param promptsRoot root of the prompts directory tree
     * @return unmodifiable list of spec file paths
     */
    public List<Path> findAllSpecs(Path promptsRoot) {
        try (Stream<Path> walk = Files.walk(promptsRoot)) {
            return walk
                    .filter(Files::isRegularFile)
                    .filter(p -> SPEC_FILENAME.equals(p.getFileName().toString()))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot walk prompts root: " + promptsRoot, e);
        }
    }

    // -----------------------------------------------------------------------
    // Internals
    // -----------------------------------------------------------------------

    /** Returns a stream of leaf directories (no child directories). */
    private Stream<Path> leafDirectories(Path root) {
        try {
            return Files.walk(root)
                    .filter(Files::isDirectory)
                    .filter(dir -> !dir.equals(root))
                    .filter(dir -> !dir.getFileName().toString().startsWith("_"))
                    .filter(this::isLeafDirectory);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot walk prompts root: " + root, e);
        }
    }

    private boolean isLeafDirectory(Path dir) {
        try (Stream<Path> children = Files.list(dir)) {
            return children.noneMatch(Files::isDirectory);
        } catch (IOException e) {
            return false;
        }
    }

    private boolean hasSpecFile(Path dir) {
        return Files.exists(dir.resolve(SPEC_FILENAME));
    }
}
