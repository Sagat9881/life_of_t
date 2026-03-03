package ru.lifegame.assets.domain.service;

import ru.lifegame.assets.domain.model.asset.AssetSpec;

import java.nio.file.Path;

/**
 * Domain-level contract for generating a layered game asset.
 *
 * <p>Implementations are responsible for producing all output artefacts
 * (PNG layers, WebP atlas, JSON config) in the given output directory.</p>
 *
 * @see ru.lifegame.assets.infrastructure.generator.LayeredAssetGenerator
 */
public interface AssetGenerationService {

    /**
     * Generate all artefacts for the supplied {@code spec} inside
     * {@code outputDir}.
     *
     * @param spec      the asset specification to generate
     * @param outputDir target directory; created if absent
     * @throws AssetGenerationException on any unrecoverable error
     */
    void generate(AssetSpec spec, Path outputDir);
}
