package ru.lifegame.assets.domain.service;

import ru.lifegame.assets.domain.model.asset.AssetSpec;

import java.nio.file.Path;
import java.util.List;

/**
 * Domain service interface for generating layered assets from specifications.
 */
public interface AssetGenerationService {

    /**
     * Generates all layers (static PNGs and animation atlases) for the given spec.
     *
     * @param spec       the asset specification parsed from XML
     * @param outputRoot root directory where generated files will be written
     * @return list of paths to all generated files
     */
    List<Path> generateAsset(AssetSpec spec, Path outputRoot);
}
