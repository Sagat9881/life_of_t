package ru.lifegame.assets.sprite;

/**
 * Identifies a single frame within a sprite animation.
 *
 * @param frameIndex  zero-based column index in the sprite-sheet strip
 * @param direction   which direction the character faces
 * @param action      which action is being animated
 */
public record AnimationFrame(int frameIndex, Direction direction, SpriteAction action) {

    public AnimationFrame {
        if (frameIndex < 0) throw new IllegalArgumentException("frameIndex must be >= 0, got " + frameIndex);
        if (direction == null) throw new IllegalArgumentException("direction must not be null");
        if (action    == null) throw new IllegalArgumentException("action must not be null");
    }

    /** Factory shorthand. */
    public static AnimationFrame of(int frameIndex, Direction direction, SpriteAction action) {
        return new AnimationFrame(frameIndex, direction, action);
    }
}
