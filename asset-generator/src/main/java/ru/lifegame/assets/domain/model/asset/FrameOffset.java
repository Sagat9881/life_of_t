package ru.lifegame.assets.domain.model.asset;

import java.util.Collections;
import java.util.Map;

/**
 * Per-frame layer offsets for animation. Each frame can shift layers by (dx, dy).
 *
 * @param frameIndex   zero-based frame index
 * @param layerOffsets map of layerId -> [dx, dy] offset
 */
public record FrameOffset(
        int frameIndex,
        Map<String, int[]> layerOffsets
) {
    public FrameOffset {
        layerOffsets = layerOffsets != null
                ? Collections.unmodifiableMap(layerOffsets)
                : Map.of();
    }

    public int[] getOffset(String layerId) {
        return layerOffsets.getOrDefault(layerId, new int[]{0, 0});
    }
}
