package ru.lifegame.assets.domain.model.asset;

import java.util.List;

/**
 * Represents a named color palette used in asset generation.
 *
 * @param name   Unique identifier for the palette (e.g., "skin_light").
 * @param colors Ordered list of hex color strings (e.g., ["#FFDFC4", "#F0C080"]).
 */
public record ColorPalette(
        String name,
        List<String> colors
) {
    public ColorPalette {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Palette name must not be blank");
        colors = List.copyOf(colors);
    }
}
