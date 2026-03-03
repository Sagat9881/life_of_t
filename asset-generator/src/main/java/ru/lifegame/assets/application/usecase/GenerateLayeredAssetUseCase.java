package ru.lifegame.assets.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.assets.domain.model.asset.AssetSpec;
import ru.lifegame.assets.domain.service.AssetGenerationService;

import java.nio.file.Path;
import java.util.List;

/**
 * Use case: generate a complete set of layered assets from a parsed AssetSpec.
 */
public class GenerateLayeredAssetUseCase {

    private static final Logger log = LoggerFactory.getLogger(GenerateLayeredAssetUseCase.class);

    private final AssetGenerationService generationService;

    public GenerateLayeredAssetUseCase(AssetGenerationService generationService) {
        this.generationService = generationService;
    }

    /**
     * Executes asset generation for the given specification.
     *
     * @param spec       parsed asset specification
     * @param outputRoot output directory root
     * @return paths to all generated files
     */
    public List<Path> execute(AssetSpec spec, Path outputRoot) {
        log.info("Generating layered asset: {}/{}", spec.entityType(), spec.entityName());
        List<Path> generated = generationService.generateAsset(spec, outputRoot);
        log.info("Generated {} files for {}/{}", generated.size(), spec.entityType(), spec.entityName());
        return generated;
    }
}
