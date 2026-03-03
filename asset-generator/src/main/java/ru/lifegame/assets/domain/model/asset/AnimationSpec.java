package ru.lifegame.assets.domain.model.asset;

import java.util.List;

/**
 * Describes an animation sequence for a sprite asset.
 *
 * <p>Each animation belongs to a named state (e.g., "idle", "walk") and
 * contains an ordered list of frame indices referencing the atlas strip.</p>
 *
 * @param state      Animation state name (e.g., "idle", "run").
 * @param frameCount Total number of frames in the sequence.
 * @param fps        Playback speed in frames per second.
 * @param loop       Whether the animation loops after the last frame.
 * @param frames     Ordered list of frame indices in the atlas.
 */
public record AnimationSpec(
        String state,
        int frameCount,
        int fps,
        boolean loop,
        List<Integer> frames
) {
    public AnimationSpec {
        if (state == null || state.isBlank()) throw new IllegalArgumentException("Animation state must not be blank");
        if (frameCount <= 0) throw new IllegalArgumentException("frameCount must be positive");
        if (fps <= 0) throw new IllegalArgumentException("fps must be positive");
        frames = List.copyOf(frames);
    }
}
