package ru.lifegame.assets.application.usecase;

import org.springframework.stereotype.Service;
import ru.lifegame.assets.infrastructure.scanner.PromptDirectoryScanner;

import java.nio.file.Path;
import java.util.List;

/**
 * Application-layer use case: scan a prompts root directory and report
 * every entity directory that is missing a {@code visual-specs.xml} file.
 *
 * <p>The result is a plain list of {@link Path} values pointing to entity
 * directories where spec authoring work is still required.</p>
 */
@Service
public class ScanMissingSpecsUseCase {

    private final PromptDirectoryScanner scanner;

    public ScanMissingSpecsUseCase(PromptDirectoryScanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Return entity directories under {@code promptsRoot} that lack a
     * {@code visual-specs.xml} file.
     *
     * @param promptsRoot root of the prompts directory tree
     * @return unmodifiable list of directories missing a spec file
     */
    public List<Path> execute(Path promptsRoot) {
        return scanner.findMissingSpecs(promptsRoot);
    }
}
