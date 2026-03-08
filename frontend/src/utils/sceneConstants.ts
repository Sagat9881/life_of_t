/**
 * Scene dimension constants — single source of truth.
 *
 * The background (location) defines the coordinate space.
 * Everything else scales relative to these dimensions.
 *
 * Background assets are rendered at exactly SCENE_WIDTH × SCENE_HEIGHT.
 * Characters, furniture, UI — all compute their display size as a
 * fraction of the scene viewport.
 */

/** Native scene width in logical pixels */
export const SCENE_WIDTH = 640;

/** Native scene height in logical pixels */
export const SCENE_HEIGHT = 480;

/** Aspect ratio of the scene */
export const SCENE_ASPECT = SCENE_WIDTH / SCENE_HEIGHT;

/**
 * Compute the CSS display height for a sprite, given its native frame
 * height and the desired fraction of the scene viewport it should occupy.
 *
 * @param frameHeight - native pixel height of one animation frame
 * @param frameWidth  - native pixel width of one animation frame
 * @param targetHeightFraction - fraction of SCENE_HEIGHT (0..1).
 *        e.g. 0.40 means the sprite occupies 40% of the viewport height.
 * @returns { displayWidth, displayHeight } in scene-logical pixels
 */
export function computeRelativeSize(
  frameWidth: number,
  frameHeight: number,
  targetHeightFraction: number
): { displayWidth: number; displayHeight: number } {
  const displayHeight = SCENE_HEIGHT * targetHeightFraction;
  const aspectRatio = frameWidth / frameHeight;
  const displayWidth = displayHeight * aspectRatio;
  return { displayWidth, displayHeight };
}

/**
 * Default height fractions for entity types.
 * These define how tall each entity type appears relative to the scene.
 *
 * The values are chosen so that:
 * - A character (128×192 canvas) at 0.40 = 192px display height
 *   → scale factor = 192/192 = 1.0 (pixel-perfect at native resolution)
 * - A cat (128×96 canvas) at 0.20 = 96px display height
 *   → scale factor = 96/96 = 1.0 (pixel-perfect)
 * - Furniture varies by item; falls back to frameHeight/SCENE_HEIGHT
 *   so that each item renders at its native pixel size within the scene.
 *
 * These are DEFAULTS. LocationConfig can override per-placement via `scale`.
 */
export const DEFAULT_HEIGHT_FRACTIONS: Record<string, number> = {
  // Characters: 192px frame → 40% of 480px = 192px (1:1 pixel mapping)
  characters: 192 / SCENE_HEIGHT,
  // Locations: background fills entire viewport
  locations: 1.0,
};

/**
 * For entity types not in DEFAULT_HEIGHT_FRACTIONS,
 * compute fraction from the frame's native height.
 * This ensures the sprite renders at 1:1 pixel scale within the scene.
 */
export function getDefaultHeightFraction(
  entityType: string,
  frameHeight: number
): number {
  return DEFAULT_HEIGHT_FRACTIONS[entityType] ?? frameHeight / SCENE_HEIGHT;
}
