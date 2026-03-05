package ru.lifegame.assets.domain.model.asset;

import java.util.Collections;
import java.util.List;

/**
 * Container for all pixel drawing primitives within a layer.
 * Primitives are drawn in order: rects first, then lines, then dots (painter's algorithm).
 *
 * @param rects filled rectangles
 * @param lines horizontal/vertical lines
 * @param dots  individual pixels
 */
public record PixelData(
        List<PixelRect> rects,
        List<PixelLine> lines,
        List<PixelDot> dots
) {
    public static final PixelData EMPTY = new PixelData(List.of(), List.of(), List.of());

    public PixelData {
        rects = rects != null ? Collections.unmodifiableList(rects) : List.of();
        lines = lines != null ? Collections.unmodifiableList(lines) : List.of();
        dots = dots != null ? Collections.unmodifiableList(dots) : List.of();
    }

    public boolean isEmpty() {
        return rects.isEmpty() && lines.isEmpty() && dots.isEmpty();
    }
}
