package ru.lifegame.assets.domain.model.asset;

/**
 * Physical size and frame constraints for a generated asset.
 *
 * @param widthPx        Canvas width in pixels.
 * @param heightPx       Canvas height in pixels.
 * @param maxFrames      Upper bound on atlas frame count.
 * @param allowTransparency Whether the output supports an alpha channel.
 * @param outputFormats  Comma-separated list of accepted output formats
 *                       (e.g., "png,webp").
 */
public record AssetConstraints(
        int widthPx,
        int heightPx,
        int maxFrames,
        boolean allowTransparency,
        String outputFormats
) {
    public AssetConstraints {
        if (widthPx <= 0)  throw new IllegalArgumentException("widthPx must be positive");
        if (heightPx <= 0) throw new IllegalArgumentException("heightPx must be positive");
        if (maxFrames <= 0) throw new IllegalArgumentException("maxFrames must be positive");
    }
}
