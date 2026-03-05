package ru.lifegame.assets.domain.model.asset;

/**
 * A single layer within a layered asset specification.
 * Now includes pixel-level drawing data parsed from XML.
 *
 * @param id          unique identifier of the layer (e.g. "head", "hair", "torso")
 * @param type        layer type: "base", "overlay", "background", etc.
 * @param description human-readable description
 * @param zOrder      rendering order — lower values rendered first (behind)
 * @param width       layer canvas width in pixels (0 = use spec default)
 * @param height      layer canvas height in pixels (0 = use spec default)
 * @param pixelData   drawing primitives for this layer
 */
public record AssetLayer(
        String id,
        String type,
        String description,
        int zOrder,
        int width,
        int height,
        PixelData pixelData
) {
    public AssetLayer {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Layer id must not be blank");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Layer type must not be blank");
        }
        if (pixelData == null) {
            pixelData = PixelData.EMPTY;
        }
    }

    /** Backward-compatible constructor for layers without pixel data. */
    public AssetLayer(String id, String type, String description, int zOrder) {
        this(id, type, description, zOrder, 0, 0, PixelData.EMPTY);
    }
}
