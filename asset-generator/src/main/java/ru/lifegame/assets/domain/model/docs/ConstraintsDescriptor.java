package ru.lifegame.assets.domain.model.docs;

/**
 * Constraints block extracted from an entity's XML spec for docs-preview output.
 *
 * @param maxColors    maximum number of colors allowed
 * @param pixelSize    pixel size (usually 1)
 * @param antiAliasing whether anti-aliasing is allowed
 */
public record ConstraintsDescriptor(
        Integer maxColors,
        Integer pixelSize,
        boolean antiAliasing
) {}
