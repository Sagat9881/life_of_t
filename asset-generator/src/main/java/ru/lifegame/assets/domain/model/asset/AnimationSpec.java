package ru.lifegame.assets.domain.model.asset;

import java.util.Collections;
import java.util.List;

/**
 * Specification for a single animation within an asset.
 *
 * @param name         animation identifier (e.g. "idle", "walk")
 * @param frames       total number of frames (1..50)
 * @param fps          frames per second for playback
 * @param loop         whether the animation loops
 * @param frameWidth   width of each frame in pixels
 * @param frameHeight  height of each frame in pixels
 * @param frameOffsets per-frame layer offsets (may be empty for static offset)
 */
public record AnimationSpec(
        String name,
        int frames,
        int fps,
        boolean loop,
        int frameWidth,
        int frameHeight,
        List<FrameOffset> frameOffsets
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
    }

    /** Backward-compatible constructor without frame offsets. */
    public AnimationSpec(String name, int frames, int fps, boolean loop,
                         int frameWidth, int frameHeight) {
        this(name, frames, fps, loop, frameWidth, frameHeight, List.of());
    }
}
