package ru.lifegame.assets;

/**
 * Classifies the kind of asset being generated.
 */
public enum AssetType {
    /** Repeating tile textures (floor, wall, fabric, etc.) */
    TEXTURE,
    /** Animated character sprite sheets */
    CHARACTER,
    /** Full scene background images */
    BACKGROUND,
    /** Inventory / world items */
    ITEM
}
