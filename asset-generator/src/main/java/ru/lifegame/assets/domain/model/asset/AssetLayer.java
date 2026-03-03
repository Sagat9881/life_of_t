package ru.lifegame.assets.domain.model.asset;

/**
 * A single layer within a layered asset specification.
 *
 * @param id          unique identifier of the layer (e.g. "background", "base", "outfit")
 * @param type        layer type: "background", "midground", "foreground", "base", "outfit", "overlay", "accessory"
 * @param description human-readable description of what this layer contains
 * @param zOrder      rendering order — lower values rendered first (behind)
 */
public record AssetLayer(
        String id,
        String type,
        String description,
        int zOrder
) {
    public AssetLayer {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Layer id must not be blank");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Layer type must not be blank");
        }
    }
}
