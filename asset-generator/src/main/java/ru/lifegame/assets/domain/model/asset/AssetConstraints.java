package ru.lifegame.assets.domain.model.asset;

/**
 * Technical constraints for asset generation.
 * Updated for HD Pixel Art assets (v2.1):
 * - Max file size raised to 1500KB for HD sprite sheets
 * - Max sprite sheet dimensions raised to 4096px for 16+ frame HD atlases
 *
 * @param maxFileSizeKb         maximum file size in KB per asset (default 1500)
 * @param maxSpriteSheetWidth   max sprite sheet width in pixels (default 4096)
 * @param maxSpriteSheetHeight  max sprite sheet height in pixels (default 4096)
 * @param compressionQuality    compression quality 0..100 (default 85)
 * @param bitDepth              bit depth (default 32 for RGBA)
 */
public record AssetConstraints(
        int maxFileSizeKb,
        int maxSpriteSheetWidth,
        int maxSpriteSheetHeight,
        int compressionQuality,
        int bitDepth
) {
    public AssetConstraints {
        if (maxFileSizeKb <= 0) throw new IllegalArgumentException("maxFileSizeKb must be positive");
        if (compressionQuality < 0 || compressionQuality > 100) {
            throw new IllegalArgumentException("compressionQuality must be 0..100");
        }
    }

    public static AssetConstraints defaults() {
        return new AssetConstraints(1500, 4096, 4096, 85, 32);
    }
}
