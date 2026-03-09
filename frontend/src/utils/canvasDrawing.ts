/**
 * Canvas 2D drawing utilities for pixel-art game scenes.
 *
 * All functions operate in SCENE logical coordinates (640×480).
 * The canvas context should already be scaled to match.
 *
 * Drawing is pixel-perfect: imageSmoothingEnabled must be false.
 */
import type { SpriteAnimation, CropOffset } from '@/types/sprite';

/* ── Constants ── */
export const SCENE_W = 640;
export const SCENE_H = 480;

/* ── Sprite frame drawing ── */

/**
 * Draw a single animation frame from a sprite atlas onto the canvas.
 *
 * @param ctx        Canvas 2D context (already in scene coordinates)
 * @param img        Preloaded atlas HTMLImageElement
 * @param anim       Resolved SpriteAnimation (from assetService)
 * @param frame      Current frame index (0-based)
 * @param x          Center-X position in scene coords (0–640)
 * @param y          Bottom-Y position in scene coords (0–480)
 * @param height     Desired display height in scene pixels
 * @param scale      Additional scale multiplier (default 1)
 * @param flipX      Mirror horizontally (default false)
 */
export function drawSpriteFrame(
  ctx: CanvasRenderingContext2D,
  img: HTMLImageElement,
  anim: SpriteAnimation,
  frame: number,
  x: number,
  y: number,
  height: number,
  scale = 1,
  flipX = false
): void {
  const fw = anim.frameWidth;
  const fh = anim.frameHeight;
  const aspectRatio = fw / fh;

  const drawH = height * scale;
  const drawW = drawH * aspectRatio;

  // Source rect in atlas
  const sx = frame * fw;
  const sy = anim.currentRow * fh;

  // Destination: x is center, y is bottom
  const dx = x - drawW / 2;
  const dy = y - drawH;

  ctx.save();

  if (flipX) {
    ctx.translate(x, 0);
    ctx.scale(-1, 1);
    ctx.translate(-x, 0);
  }

  ctx.drawImage(img, sx, sy, fw, fh, dx, dy, drawW, drawH);
  ctx.restore();
}

/* ── Background ── */

/**
 * Draw a background frame (fills entire scene).
 */
export function drawBackground(
  ctx: CanvasRenderingContext2D,
  img: HTMLImageElement,
  anim: SpriteAnimation,
  frame: number
): void {
  const fw = anim.frameWidth;
  const fh = anim.frameHeight;
  const sx = frame * fw;
  const sy = anim.currentRow * fh;
  ctx.drawImage(img, sx, sy, fw, fh, 0, 0, SCENE_W, SCENE_H);
}

/* ── Ambient overlay ── */

export interface AmbientConfig {
  color: string;
  opacity: number;
}

const AMBIENT_MAP: Record<string, AmbientConfig> = {
  morning: { color: '#FFE8C0', opacity: 0.08 },
  day:     { color: '#FFFFF0', opacity: 0.0 },
  evening: { color: '#FF8040', opacity: 0.18 },
  night:   { color: '#101830', opacity: 0.50 },
};

/**
 * Draw time-of-day ambient color overlay.
 */
export function drawAmbientOverlay(
  ctx: CanvasRenderingContext2D,
  timeOfDay: string
): void {
  const ambient = AMBIENT_MAP[timeOfDay] ?? AMBIENT_MAP['day']!;
  if (ambient.opacity <= 0) return;

  ctx.save();
  ctx.globalAlpha = ambient.opacity;
  ctx.globalCompositeOperation = 'multiply';
  ctx.fillStyle = ambient.color;
  ctx.fillRect(0, 0, SCENE_W, SCENE_H);
  ctx.restore();
}

/* ── Selection glow ── */

/**
 * Draw a subtle glow around a selected/hovered entity.
 */
export function drawSelectionGlow(
  ctx: CanvasRenderingContext2D,
  x: number,
  y: number,
  width: number,
  height: number,
  color = 'rgba(240, 147, 251, 0.5)'
): void {
  ctx.save();
  ctx.shadowColor = color;
  ctx.shadowBlur = 8;
  ctx.strokeStyle = color;
  ctx.lineWidth = 1;
  ctx.strokeRect(x - width / 2 - 2, y - height - 2, width + 4, height + 4);
  ctx.restore();
}

/* ── Label ── */

/**
 * Draw a pixel-font label above an entity.
 */
export function drawLabel(
  ctx: CanvasRenderingContext2D,
  text: string,
  x: number,
  y: number,
  entityHeight: number
): void {
  ctx.save();
  ctx.font = '8px "Press Start 2P", monospace';
  ctx.textAlign = 'center';
  ctx.textBaseline = 'bottom';

  const metrics = ctx.measureText(text);
  const padX = 6;
  const padY = 3;
  const tw = metrics.width + padX * 2;
  const th = 8 + padY * 2;
  const lx = x - tw / 2;
  const ly = y - entityHeight - 8 - th;

  // Background pill
  ctx.fillStyle = 'rgba(0, 0, 0, 0.9)';
  ctx.fillRect(lx, ly, tw, th);
  ctx.strokeStyle = '#4a3f5d';
  ctx.lineWidth = 1;
  ctx.strokeRect(lx, ly, tw, th);

  // Text
  ctx.fillStyle = '#ffffff';
  ctx.fillText(text, x, ly + th - padY);
  ctx.restore();
}

/* ── Hit testing ── */

export interface HitBox {
  id: string;
  actionCode?: string;
  label?: string;
  x: number;       // center X in scene coords
  y: number;       // bottom Y in scene coords
  width: number;   // display width
  height: number;  // display height
  zOrder: number;
}

/**
 * Test if a scene-coordinate point hits any entity.
 * Returns the topmost (highest zOrder) hit, or null.
 */
export function hitTest(
  sceneX: number,
  sceneY: number,
  hitBoxes: HitBox[]
): HitBox | null {
  // Sort by zOrder descending — topmost first
  const sorted = [...hitBoxes].sort((a, b) => b.zOrder - a.zOrder);

  for (const box of sorted) {
    const left = box.x - box.width / 2;
    const top = box.y - box.height;
    const right = left + box.width;
    const bottom = box.y;

    if (sceneX >= left && sceneX <= right && sceneY >= top && sceneY <= bottom) {
      return box;
    }
  }

  return null;
}
