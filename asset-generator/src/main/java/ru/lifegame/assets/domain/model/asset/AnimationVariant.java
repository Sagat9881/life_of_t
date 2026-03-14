package ru.lifegame.assets.domain.model.asset;

import ru.lifegame.assets.domain.model.asset.AtlasConfigSchema.SingleCondition;

import java.util.List;

/**
 * A single grid-row variant of an animation, carrying its own predicate conditions
 * and playback parameters.
 *
 * @param conditions  predicate list evaluated by the frontend to select this row;
 *                    empty list means always-true (default row)
 * @param fps         frames per second for this variant
 * @param loop        whether this variant loops
 * @param frameOffsets per-frame layer offsets for this variant (may be empty)
 */
public record AnimationVariant(
        List<SingleCondition> conditions,
        int fps,
        boolean loop,
        List<FrameOffset> frameOffsets
) {
    public AnimationVariant {
        conditions = conditions != null ? List.copyOf(conditions) : List.of();
        frameOffsets = frameOffsets != null ? List.copyOf(frameOffsets) : List.of();
    }
}
