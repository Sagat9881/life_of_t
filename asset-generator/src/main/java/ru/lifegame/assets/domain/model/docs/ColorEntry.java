package ru.lifegame.assets.domain.model.docs;

/**
 * A single color entry in an entity's palette descriptor.
 *
 * @param name  symbolic name of the color (e.g. "$skin_base")
 * @param hex   hex color value (e.g. "#F0C098")
 */
public record ColorEntry(String name, String hex) {
    public ColorEntry {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (hex  == null || hex.isBlank())  throw new IllegalArgumentException("hex must not be blank");
    }
}
