/**
 * PixelSceneCanvas Component
 *
 * Renders a pixel-art game location using the Canvas API.
 * Canvas buffer is dynamically sized to fill the container with 4:3 letterbox/pillarbox.
 *
 * Coordinate system:
 * - All x/y/sceneHeight values in LocationConfig are 0–100 (% of viewport).
 * - Hit detection mirrors the renderer's coordinate computation via viewportRef.
 *
 * Layers (bottom → top): background → furniture (z-sorted) → characters (z-sorted).
 */

import { useRef, useEffect } from 'react';
import { useCanvasRenderer } from '../../hooks/useCanvasRenderer';
import type { LocationConfig, FurniturePlacement } from '../../types/location.types';
import type { GameStateSnapshot } from '../../hooks/canvasTypes';

interface PixelSceneCanvasProps {
  config: LocationConfig;
  gameState: GameStateSnapshot;
  selectedObjectId: string | null;
  hoveredObjectId: string | null;
  characterAnimations?: Record<string, string>;
  onObjectClick: (objectId: string | null) => void;
  onObjectHover: (objectId: string | null) => void;
}

const LOGICAL_W = 640;
const LOGICAL_H = 480;
const ASPECT = LOGICAL_W / LOGICAL_H; // 4/3

/**
 * Вычисляет viewport в пикселях холста с letterbox/pillarbox для сохранения 4:3.
 */
function computeViewport(cssW: number, cssH: number) {
  const cssAspect = cssW / cssH;
  let vpW: number, vpH: number, vpX: number, vpY: number;
  if (cssAspect >= ASPECT) {
    // шире чем 4:3 — pillarbox (полосы по бокам)
    vpH = cssH;
    vpW = cssH * ASPECT;
    vpX = (cssW - vpW) / 2;
    vpY = 0;
  } else {
    // уже чем 4:3 — letterbox (полосы сверху/снизу)
    vpW = cssW;
    vpH = cssW / ASPECT;
    vpX = 0;
    vpY = (cssH - vpH) / 2;
  }
  return { vpX, vpY, vpW, vpH };
}

function getFurnitureHitBox(
  f: FurniturePlacement,
  vpX: number, vpY: number, vpW: number, vpH: number
): { x: number; y: number; width: number; height: number } {
  const cx = vpX + (f.x / 100) * vpW;
  const cy = vpY + (f.y / 100) * vpH;
  const height = (f.sceneHeight / 100) * vpH * f.scale;
  const width = height * 1.2;
  return { x: cx - width / 2, y: cy - height, width, height };
}

export function PixelSceneCanvas({
  config,
  gameState,
  selectedObjectId,
  hoveredObjectId,
  characterAnimations,
  onObjectClick,
  onObjectHover,
}: PixelSceneCanvasProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const viewportRef = useRef({ vpX: 0, vpY: 0, vpW: LOGICAL_W, vpH: LOGICAL_H });

  // ResizeObserver — подстраиваем canvas buffer и viewport под контейнер
  useEffect(() => {
    const container = containerRef.current;
    const canvas = canvasRef.current;
    if (!container || !canvas) return;

    const observer = new ResizeObserver((entries) => {
      const entry = entries[0];
      if (!entry) return;
      const { width, height } = entry.contentRect;
      const dpr = window.devicePixelRatio ?? 1;
      const bufW = Math.round(width * dpr);
      const bufH = Math.round(height * dpr);
      canvas.width = bufW;
      canvas.height = bufH;
      viewportRef.current = computeViewport(bufW, bufH);
    });

    observer.observe(container);
    return () => observer.disconnect();
  }, []);

  useCanvasRenderer({
    config,
    canvasRef,
    viewportRef,
    gameState,
    selectedObjectId,
    hoveredObjectId,
    ...(characterAnimations !== undefined && { characterAnimations }),
  });

  const toCanvasCoords = (e: React.MouseEvent<HTMLCanvasElement>) => {
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

  const hitTest = (coords: { x: number; y: number }): FurniturePlacement | undefined => {
    const { vpX, vpY, vpW, vpH } = viewportRef.current;
    return config.furniture.find((f) => {
      const hb = getFurnitureHitBox(f, vpX, vpY, vpW, vpH);
      return (
        coords.x >= hb.x && coords.x <= hb.x + hb.width &&
        coords.y >= hb.y && coords.y <= hb.y + hb.height
      );
    });
  };

  const handleClick = (e: React.MouseEvent<HTMLCanvasElement>) => {
    const coords = toCanvasCoords(e);
    if (!coords) return;
    onObjectClick(hitTest(coords)?.id ?? null);
  };

  const handleMove = (e: React.MouseEvent<HTMLCanvasElement>) => {
    const coords = toCanvasCoords(e);
    if (!coords) return;
    onObjectHover(hitTest(coords)?.id ?? null);
  };

  return (
    <div
      ref={containerRef}
      style={{ width: '100%', height: '100%', overflow: 'hidden', background: '#1a1a2e' }}
    >
      <canvas
        ref={canvasRef}
        width={LOGICAL_W}
        height={LOGICAL_H}
        onClick={handleClick}
        onMouseMove={handleMove}
        onMouseLeave={() => onObjectHover(null)}
        style={{
          display: 'block',
          width: '100%',
          height: '100%',
          imageRendering: 'pixelated',
          cursor: hoveredObjectId ? 'pointer' : 'default',
        }}
      />
    </div>
  );
}
