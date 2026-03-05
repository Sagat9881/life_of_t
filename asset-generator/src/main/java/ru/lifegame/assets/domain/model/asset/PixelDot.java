package ru.lifegame.assets.domain.model.asset;

/**
 * A single pixel primitive for pixel-art layer data.
 *
 * @param x     x coordinate
 * @param y     y coordinate
 * @param color hex color string (e.g. "#30A040")
 */
public record PixelDot(int x, int y, String color) {
    public PixelDot {
        if (color == null || color.isBlank()) {
            throw new IllegalArgumentException("PixelDot color must not be blank");
        }
    }
}
