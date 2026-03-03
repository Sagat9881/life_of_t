package ru.lifegame.assets;

import java.awt.image.BufferedImage;

/**
 * Base interface for all asset generators.
 * Implementations must be deterministic for the same AssetRequest.
 */
public interface AssetGenerator {

    /**
     * Returns the human-readable name of this generator.
     */
    String name();

    /**
     * Generates a BufferedImage for the given request.
     *
     * @param request the asset generation parameters
     * @return generated image of dimensions (request.width x request.height)
     */
    BufferedImage generate(AssetRequest request);
}
