package ru.lifegame.assets.infrastructure.parser;

import ru.lifegame.assets.domain.model.asset.LayerCondition;
import ru.lifegame.assets.domain.model.asset.PixelData;

import java.util.List;

/**
 * Represents a layer override from a child spec's layer-overrides block.
 * When replace=true, the entire parent layer is replaced.
 * When replace=false, pixel data is merged (child appended to parent).
 *
 * @param id         layer id to override (must match a parent layer id)
 * @param replace    true = full replacement, false = merge pixel data
 * @param type       override type (blank = inherit from parent)
 * @param zOrder     override z-order (-1 = inherit from parent)
 * @param width      override width (0 = inherit from parent)
 * @param height     override height (0 = inherit from parent)
 * @param pixelData  pixel data for this override
 * @param conditions condition overrides
 */
public record LayerOverride(
        String id,
        boolean replace,
        String type,
        int zOrder,
        int width,
        int height,
        PixelData pixelData,
        List<LayerCondition> conditions
) {
    public LayerOverride {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("LayerOverride id must not be blank");
        }
        if (pixelData == null) pixelData = PixelData.EMPTY;
        if (conditions == null) conditions = List.of();
    }
}
