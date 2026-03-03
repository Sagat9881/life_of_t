package ru.lifegame.assets.domain.model.asset;

/**
 * Represents a single visual layer within an asset.
 *
 * @param name        Unique layer name within the asset (e.g., "base", "eyes").
 * @param type        Layer type hint: "base", "overlay", "shadow", etc.
 * @param paletteRef  Optional reference to a {@link ColorPalette} name.
 * @param zIndex      Rendering order; lower values render first (behind).
 * @param optional    Whether the layer can be omitted during generation.
 */
public record AssetLayer(
        String name,
        String type,
        String paletteRef,
        int zIndex,
        boolean optional
) {
    public AssetLayer {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Layer name must not be blank");
        if (type == null || type.isBlank()) throw new IllegalArgumentException("Layer type must not be blank");
    }
}
