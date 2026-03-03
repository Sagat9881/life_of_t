package ru.lifegame.assets.sprite;

/**
 * A single compositable layer of a character sprite.
 *
 * @param name      human-readable identifier for this layer (e.g. "female_base")
 * @param category  what part of the character this layer represents
 * @param imagePath file-system path (or classpath resource path) of the
 *                  LPC sprite-sheet image for this layer; may be null or
 *                  point to a non-existent file — the compositor will then
 *                  generate a coloured placeholder silhouette
 * @param zOrder    rendering order — higher values appear on top;
 *                  typically derived from {@link SpriteLayerCategory#ordinal()}
 */
public record SpriteLayer(
        String name,
        SpriteLayerCategory category,
        String imagePath,
        int zOrder
) {
    public SpriteLayer {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("layer name must not be blank");
        if (category == null)               throw new IllegalArgumentException("category must not be null");
        // imagePath may be null — triggers placeholder rendering
    }

    /**
     * Convenience factory that derives {@code zOrder} from the category's ordinal.
     */
    public static SpriteLayer of(String name, SpriteLayerCategory category, String imagePath) {
        return new SpriteLayer(name, category, imagePath, category.ordinal());
    }
}
