package ru.lifegame.assets.application.usecase;

import org.springframework.stereotype.Service;
import ru.lifegame.assets.infrastructure.scanner.PromptDirectoryScanner;
import ru.lifegame.assets.infrastructure.parser.XmlAssetSpecParser;
import ru.lifegame.assets.domain.model.asset.AssetSpec;

import java.nio.file.Path;
import java.util.List;

/**
 * Application-layer use case: collect and validate all {@code visual-specs.xml}
 * files found beneath a prompts root directory.
 *
 * <p>Returns a list of successfully parsed {@link AssetSpec} objects.  Any
 * file that cannot be parsed is logged and skipped; it does not abort the
 * entire scan.</p>
 */
@Service
public class UnifyXmlSpecsUseCase {

    private final PromptDirectoryScanner scanner;
    private final XmlAssetSpecParser     parser;

    public UnifyXmlSpecsUseCase(PromptDirectoryScanner scanner,
                                XmlAssetSpecParser parser) {
        this.scanner = scanner;
        this.parser  = parser;
    }

    /**
     * Scan {@code promptsRoot} for {@code visual-specs.xml} files and parse
     * each one.
     *
     * @param promptsRoot root of the prompts directory tree
     * @return list of successfully parsed {@link AssetSpec} instances
     */
    public List<AssetSpec> execute(Path promptsRoot) {
        return scanner.findAllSpecs(promptsRoot)
                      .stream()
                      .map(parser::parse)
                      .toList();
    }
}
