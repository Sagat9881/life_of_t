package ru.lifegame.assets.application.usecase;

import org.springframework.stereotype.Service;
import ru.lifegame.assets.domain.model.asset.AssetSpec;
import ru.lifegame.assets.domain.service.AssetGenerationService;
import ru.lifegame.assets.infrastructure.parser.XmlAssetSpecParser;

import java.nio.file.Path;

/**
 * Application-layer use case: parse an XML spec file and generate the asset.
 *
 * <p>Wires together {@link XmlAssetSpecParser} (infrastructure) and
 * {@link AssetGenerationService} (domain) so that callers never depend on
 * either concrete implementation directly.</p>
 */
@Service
public class GenerateLayeredAssetUseCase {

    private final XmlAssetSpecParser parser;
    private final AssetGenerationService generator;

    public GenerateLayeredAssetUseCase(XmlAssetSpecParser parser,
                                       AssetGenerationService generator) {
        this.parser    = parser;
        this.generator = generator;
    }

    /**
     * Parse {@code specFile} and write all artefacts to {@code outputDir}.
     *
     * @param specFile  path to a {@code visual-specs.xml} file
     * @param outputDir target directory for generated output
     */
    public void execute(Path specFile, Path outputDir) {
        AssetSpec spec = parser.parse(specFile);
        generator.generate(spec, outputDir);
    }
}
