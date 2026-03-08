package ru.lifegame.assets.infrastructure.parser;

/**
 * A color remapping entry: all pixels with color {@code from}
 * will be changed to color {@code to} during inheritance resolution.
 *
 * @param from source hex color (e.g. "#F0C098")
 * @param to   target hex color (e.g. "#D4A574")
 */
public record ColorRemap(String from, String to) {
    public ColorRemap {
        if (from == null || from.isBlank()) {
            throw new IllegalArgumentException("ColorRemap 'from' must not be blank");
        }
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("ColorRemap 'to' must not be blank");
        }
    }
}
