package ru.lifegame.assets.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.assets.infrastructure.scanner.PromptDirectoryScanner;

import java.nio.file.Path;
import java.util.List;

/**
 * Use case: scan docs/prompts/ directory tree and identify entities that
 * are missing unified XML specifications.
 */
public class ScanMissingSpecsUseCase {

    private static final Logger log = LoggerFactory.getLogger(ScanMissingSpecsUseCase.class);

    private final PromptDirectoryScanner scanner;

    public ScanMissingSpecsUseCase(PromptDirectoryScanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Scans the prompts root and returns entity directories missing a unified XML spec.
     *
     * @param promptsRoot root of docs/prompts/
     * @return list of entity directory paths that lack a visual-specs.xml
     */
    public List<Path> execute(Path promptsRoot) {
        log.info("Scanning for missing specs in: {}", promptsRoot);
        List<Path> missing = scanner.findMissingSpecs(promptsRoot);
        log.info("Found {} entities with missing specs", missing.size());
        return missing;
    }
}
