package ru.lifegame.assets.domain.model.asset;

/**
 * A horizontal or vertical line primitive for pixel-art layer data.
 *
 * @param x         start x coordinate
 * @param y         start y coordinate
 * @param length    length in pixels
 * @param direction HORIZONTAL or VERTICAL
 * @param color     hex color string
 */
public record PixelLine(int x, int y, int length, Direction direction, String color) {

    public enum Direction { HORIZONTAL, VERTICAL }

    public PixelLine {
        if (length < 1) {
            throw new IllegalArgumentException("PixelLine length must be positive");
        }
        if (color == null || color.isBlank()) {
            throw new IllegalArgumentException("PixelLine color must not be blank");
        }
        if (direction == null) {
            throw new IllegalArgumentException("PixelLine direction must not be null");
        }
    }
}
