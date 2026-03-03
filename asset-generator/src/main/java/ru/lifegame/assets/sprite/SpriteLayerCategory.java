package ru.lifegame.assets.sprite;

/**
 * Logical layer categories for a character sprite.
 * Lower ordinal values are rendered first (i.e. behind higher-ordinal layers
 * when using z-order based on category ordinal).
 */
public enum SpriteLayerCategory {
    BASE_BODY,
    SHOES,
    CLOTHING_BOTTOM,
    CLOTHING_TOP,
    EYES,
    HAIR,
    ACCESSORY
}
