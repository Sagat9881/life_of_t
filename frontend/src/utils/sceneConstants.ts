/**
 * Scene dimension constants — single source of truth.
 *
 * Canvas 2D renders at SCENE_WIDTH × SCENE_HEIGHT logical pixels.
 * The canvas auto-scales to fill its container while maintaining aspect ratio.
 *
 * All entity positions use percentages (0–100) of scene dimensions.
 * Entity sizes use sceneHeight: percentage (0–100) of scene height.
 */

/** Native scene width in logical pixels */
export const SCENE_WIDTH = 640;

/** Native scene height in logical pixels */
export const SCENE_HEIGHT = 480;

/** Aspect ratio of the scene */
export const SCENE_ASPECT = SCENE_WIDTH / SCENE_HEIGHT;

/**
 * 1% of scene height in logical pixels.
 * Usage: entityPixelHeight = sceneHeight * SCENE_UNIT_Y
 */
export const SCENE_UNIT_Y = SCENE_HEIGHT / 100;

/**
 * 1% of scene width in logical pixels.
 * Usage: entityPixelX = xPercent * SCENE_UNIT_X
 */
export const SCENE_UNIT_X = SCENE_WIDTH / 100;

/**
 * @deprecated Kept for backward compat with SpriteAnimator.
 * Canvas rendering uses sceneHeight (0-100) directly.
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
 * @deprecated Kept for backward compat with SpriteAnimator.
 * Canvas rendering uses sceneHeight from LocationConfig directly.
 */
export const DEFAULT_HEIGHT_FRACTIONS: Record<string, number> = {
  characters: 192 / SCENE_HEIGHT,
  locations: 1.0,
};

/**
 * @deprecated Kept for backward compat with SpriteAnimator.
 */
export function getDefaultHeightFraction(
  entityType: string,
  frameHeight: number
): number {
  return DEFAULT_HEIGHT_FRACTIONS[entityType] ?? frameHeight / SCENE_HEIGHT;
}
