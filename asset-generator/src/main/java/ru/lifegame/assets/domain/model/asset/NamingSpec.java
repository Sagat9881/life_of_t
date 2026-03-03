package ru.lifegame.assets.domain.model.asset;

/**
 * Naming conventions applied when writing output files.
 *
 * @param prefix       Short token prepended to every output filename
 *                     (e.g., "tanya").
 * @param layerPattern Printf-style pattern for layer filenames
 *                     (e.g., "%s_layer_%02d.png").
 * @param atlasPattern Printf-style pattern for atlas filenames
 *                     (e.g., "%s_atlas.webp").
 * @param configPattern Printf-style pattern for JSON config filenames
 *                      (e.g., "%s_atlas.json").
 */
public record NamingSpec(
        String prefix,
        String layerPattern,
        String atlasPattern,
        String configPattern
) {
    public NamingSpec {
        if (prefix == null || prefix.isBlank())
            throw new IllegalArgumentException("prefix must not be blank");
    }
}
