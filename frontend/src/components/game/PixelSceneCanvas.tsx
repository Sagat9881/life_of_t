/**
 * PixelSceneCanvas Component
 *
 * Renders a pixel-art game location using the Canvas API.
 *
 * Coordinate system:
 * - All x/y/sceneHeight values in LocationConfig are 0–100 (% of canvas).
 * - Hit detection mirrors the renderer's coordinate computation.
 *
 * Layers (bottom → top): background → furniture (z-sorted) → characters (z-sorted).
 */

import { useRef } from 'react';
import { useCanvasRenderer } from '../../hooks/useCanvasRenderer';
import type { LocationConfig, FurniturePlacement } from '../../types/location.types';

interface PixelSceneCanvasProps {
  config: LocationConfig;
  timeOfDay: string;
  selectedObjectId: string | null;
  hoveredObjectId: string | null;
  characterAnimations?: Record<string, { animationName: string; frameIndex: number }> | undefined;
  onObjectClick: (objectId: string | null) => void;
  onObjectHover: (objectId: string | null) => void;
}

/**
 * Returns canvas-pixel coordinates of the hit-box centre for a furniture item.
 * Matches the draw logic in useCanvasRenderer: anchor = (x%, y%) = bottom-centre.
 * Width uses a 1:1 fallback (square) since we don't have atlas metadata here.
 */
function getFurnitureHitBox(
  f: FurniturePlacement,
  canvasWidth: number,
  canvasHeight: number
): { x: number; y: number; width: number; height: number } {
  const cx = (f.x / 100) * canvasWidth;
  const cy = (f.y / 100) * canvasHeight;
  const height = (f.sceneHeight / 100) * canvasHeight * f.scale;
  const width = height; // 1:1 fallback — atlas aspect ratio not available here
  return { x: cx - width / 2, y: cy - height, width, height };
}

export function PixelSceneCanvas({
  config,
  timeOfDay,
  selectedObjectId,
  hoveredObjectId,
  characterAnimations,
  onObjectClick,
  onObjectHover,
}: PixelSceneCanvasProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useCanvasRenderer({
    config,
    canvasRef,
    timeOfDay,
    selectedObjectId,
    hoveredObjectId,
    characterAnimations,
  });

  /** Convert a MouseEvent to internal canvas coordinates. */
  const toCanvasCoords = (
    e: React.MouseEvent<HTMLCanvasElement>
  ): { x: number; y: number } | null => {
    const canvas = canvasRef.current;
    if (!canvas) return null;
    const rect = canvas.getBoundingClientRect();
    const scaleX = canvas.width / rect.width;
    const scaleY = canvas.height / rect.height;
    return {
      x: (e.clientX - rect.left) * scaleX,
      y: (e.clientY - rect.top) * scaleY,
    };
  };

  const hitTest = (
    coords: { x: number; y: number },
    canvasWidth: number,
    canvasHeight: number
  ): FurniturePlacement | undefined =>
    config.furniture.find((f: FurniturePlacement) => {
      const hb = getFurnitureHitBox(f, canvasWidth, canvasHeight);
      return (
        coords.x >= hb.x &&
        coords.x <= hb.x + hb.width &&
        coords.y >= hb.y &&
        coords.y <= hb.y + hb.height
      );
    });

  const handleCanvasClick = (e: React.MouseEvent<HTMLCanvasElement>): void => {
    const canvas = canvasRef.current;
    const coords = toCanvasCoords(e);
    if (!canvas || !coords) return;
    const hit = hitTest(coords, canvas.width, canvas.height);
    onObjectClick(hit?.id ?? null);
  };

  const handleCanvasMove = (e: React.MouseEvent<HTMLCanvasElement>): void => {
    const canvas = canvasRef.current;
    const coords = toCanvasCoords(e);
    if (!canvas || !coords) return;
    const hit = hitTest(coords, canvas.width, canvas.height);
    onObjectHover(hit?.id ?? null);
  };

  return (
    <canvas
      ref={canvasRef}
      width={640}
      height={480}
      onClick={handleCanvasClick}
      onMouseMove={handleCanvasMove}
      onMouseLeave={() => onObjectHover(null)}
      style={{
        width: '100%',
        height: 'auto',
        imageRendering: 'pixelated',
        cursor: hoveredObjectId ? 'pointer' : 'default',
      }}
    />
  );
}
