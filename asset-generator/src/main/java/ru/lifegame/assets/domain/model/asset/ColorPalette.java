package ru.lifegame.assets.domain.model.asset;

import java.util.Collections;
import java.util.List;

/**
 * Immutable color palette specification for asset generation.
 *
 * @param primary   primary palette colors as hex strings (e.g. "#F5E6D3")
 * @param secondary secondary palette colors as hex strings
 */
public record ColorPalette(
        List<String> primary,
        List<String> secondary
) {
    public ColorPalette {
        primary = primary != null ? Collections.unmodifiableList(primary) : List.of();
        secondary = secondary != null ? Collections.unmodifiableList(secondary) : List.of();
    }

    /** Default project palette from character-visual-specs. */
    public static ColorPalette projectDefault() {
        return new ColorPalette(
                List.of("#F5E6D3", "#FFB6C1", "#B5EAD7", "#FFF4B8", "#E0BBE4"),
                List.of("#6B4423", "#4A5859", "#FFF9E9", "#A8B2B7")
        );
    }
}
