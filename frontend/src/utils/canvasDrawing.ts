/**
 * Canvas Drawing Utilities
 * 
 * Low-level helpers for sprite rendering, scaling, and effects.
 */

export interface SpriteFrame {
  x: number;
  y: number;
  width: number;
  height: number;
}

/**
 * Draw a sprite from an atlas onto canvas
 */
export function drawSprite(
  ctx: CanvasRenderingContext2D,
  image: HTMLImageElement,
  frame: SpriteFrame,
  destX: number,
  destY: number,
  scale: number = 1
) {
  ctx.drawImage(
    image,
    frame.x,
    frame.y,
    frame.width,
    frame.height,
    destX,
    destY,
    frame.width * scale,
    frame.height * scale
  );
}

/**
 * Apply tint to canvas area (for time-of-day effects)
 */
export function applyTint(
  ctx: CanvasRenderingContext2D,
  x: number,
  y: number,
  width: number,
  height: number,
  color: string,
  alpha: number
) {
  ctx.save();
  ctx.globalAlpha = alpha;
  ctx.fillStyle = color;
  ctx.fillRect(x, y, width, height);
  ctx.restore();
}

/**
 * Draw outline around sprite (for selection/hover effects)
 */
export function drawOutline(
  ctx: CanvasRenderingContext2D,
  x: number,
  y: number,
  width: number,
  height: number,
  color: string,
  thickness: number = 2
) {
  ctx.save();
  ctx.strokeStyle = color;
  ctx.lineWidth = thickness;
  ctx.strokeRect(x, y, width, height);
  ctx.restore();
}
