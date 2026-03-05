package ru.lifegame.assets.domain.model.asset;

/**
 * A filled rectangle primitive for pixel-art layer data.
 *
 * @param x      left edge in pixels
 * @param y      top edge in pixels
 * @param width  width in pixels
 * @param height height in pixels
 * @param color  hex color string (e.g. "#F0C098")
 */
public record PixelRect(int x, int y, int width, int height, String color) {
    public PixelRect {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("PixelRect dimensions must be positive");
        }
        if (color == null || color.isBlank()) {
            throw new IllegalArgumentException("PixelRect color must not be blank");
        }
    }
}
