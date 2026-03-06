package ru.lifegame.assets.domain.model.asset;

import java.util.Collections;
import java.util.List;

/**
 * A single layer within a layered asset specification.
 * Now includes pixel-level drawing data parsed from XML
 * and optional conditions for overlay layers.
 *
 * @param id          unique identifier of the layer (e.g. "head", "hair", "ambient_light")
 * @param type        layer type: "base", "overlay", "background", "foreground", etc.
 * @param description human-readable description
 * @param zOrder      rendering order — lower values rendered first (behind)
 * @param width       layer canvas width in pixels (0 = use spec default)
 * @param height      layer canvas height in pixels (0 = use spec default)
 * @param pixelData   drawing primitives for this layer
 * @param conditions  condition overrides for this layer (e.g. time-of-day tint/opacity)
 */
public record AssetLayer(
        String id,
        String type,
        String description,
        int zOrder,
        int width,
        int height,
        PixelData pixelData,
        List<LayerCondition> conditions
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
        conditions = conditions != null
                ? Collections.unmodifiableList(conditions)
                : List.of();
    }

    /** Backward-compatible constructor without conditions. */
    public AssetLayer(String id, String type, String description, int zOrder,
                      int width, int height, PixelData pixelData) {
        this(id, type, description, zOrder, width, height, pixelData, List.of());
    }

    /** Backward-compatible constructor for layers without pixel data. */
    public AssetLayer(String id, String type, String description, int zOrder) {
        this(id, type, description, zOrder, 0, 0, PixelData.EMPTY, List.of());
    }

    /** Whether this layer has condition overrides (e.g. ambient_light with time-of-day). */
    public boolean hasConditions() {
        return conditions != null && !conditions.isEmpty();
    }
}
