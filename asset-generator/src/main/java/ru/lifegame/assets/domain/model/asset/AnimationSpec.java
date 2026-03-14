package ru.lifegame.assets.domain.model.asset;

import java.util.Collections;
import java.util.List;

/**
 * Specification for a single animation within an asset.
 *
 * @param name         animation identifier (e.g. "idle", "walk")
 * @param frames       total number of frames (1..50)
 * @param fps          default frames per second (used when variants is empty)
 * @param loop         default loop flag (used when variants is empty)
 * @param frameWidth   width of each frame in pixels
 * @param frameHeight  height of each frame in pixels
 * @param frameOffsets per-frame layer offsets (used when variants is empty)
 * @param variants     ordered list of grid-row variants with predicate conditions;
 *                     empty means single-row default (use fps/loop/frameOffsets above)
 */
public record AnimationSpec(
        String name,
        int frames,
        int fps,
        boolean loop,
        int frameWidth,
        int frameHeight,
        List<FrameOffset> frameOffsets,
        List<AnimationVariant> variants
) {
    public static final int MAX_FRAMES = 50;

    public AnimationSpec {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Animation name must not be blank");
        }
        if (frames < 1 || frames > MAX_FRAMES) {
            throw new IllegalArgumentException(
                    "frames must be between 1 and " + MAX_FRAMES + ", got " + frames);
        }
        if (fps < 1) {
            throw new IllegalArgumentException("fps must be positive, got " + fps);
        }
        if (frameWidth < 1 || frameHeight < 1) {
            throw new IllegalArgumentException("frameWidth and frameHeight must be positive");
        }
        frameOffsets = frameOffsets != null
                ? Collections.unmodifiableList(frameOffsets)
                : List.of();
        variants = variants != null ? List.copyOf(variants) : List.of();
    }

    /** Backward-compatible constructor without variants. */
    public AnimationSpec(String name, int frames, int fps, boolean loop,
                         int frameWidth, int frameHeight,
                         List<FrameOffset> frameOffsets) {
        this(name, frames, fps, loop, frameWidth, frameHeight, frameOffsets, List.of());
    }

    /** Backward-compatible constructor without frame offsets or variants. */
    public AnimationSpec(String name, int frames, int fps, boolean loop,
                         int frameWidth, int frameHeight) {
        this(name, frames, fps, loop, frameWidth, frameHeight, List.of(), List.of());
    }
}
