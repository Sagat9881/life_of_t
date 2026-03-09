/**
 * PixelSceneCanvas — HTML5 Canvas 2D game scene renderer.
 *
 * Replaces the CSS-based PixelScene + SpriteAnimator pipeline with a single
 * <canvas> element that draws all layers directly via Canvas 2D API.
 *
 * The canvas has a fixed logical resolution of 640×480 and auto-scales
 * to fit its container while maintaining 4:3 aspect ratio and pixel-perfect
 * rendering (imageSmoothingEnabled = false, image-rendering: pixelated).
 *
 * All entity positions use percentage coordinates (0–100) mapped to the
 * 640×480 logical space. Entity sizes use sceneHeight (0–100) = % of
 * scene height.
 */
import { memo, useRef, useCallback, useState } from 'react';
import { useCanvasRenderer } from '@/hooks/useCanvasRenderer';
import type { LocationConfig } from '@/config/locations';
import './PixelSceneCanvas.css';

const CANVAS_W = 640;
const CANVAS_H = 480;

export interface PixelSceneCanvasProps {
  readonly config: LocationConfig;
  readonly selectedObjectId?: string | null;
  readonly onObjectClick?: (objectId: string, actionCode: string) => void;
  readonly characterAnimations?: Record<string, string>;
  readonly timeOfDay?: string;
}

export const PixelSceneCanvas = memo(function PixelSceneCanvas({
  config,
  selectedObjectId,
  onObjectClick,
  characterAnimations,
  timeOfDay = 'day',
}: PixelSceneCanvasProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [hoveredId, setHoveredId] = useState<string | null>(null);

  const { handleCanvasClick, handleCanvasMove } = useCanvasRenderer({
    config,
    canvasRef,
    timeOfDay,
    selectedObjectId,
    hoveredObjectId: hoveredId,
    characterAnimations,
  });

  const onClick = useCallback((e: React.MouseEvent<HTMLCanvasElement>) => {
    const hit = handleCanvasClick(e);
    if (hit?.actionCode && onObjectClick) {
      onObjectClick(hit.id, hit.actionCode);
    }
  }, [handleCanvasClick, onObjectClick]);

  const onMove = useCallback((e: React.MouseEvent<HTMLCanvasElement>) => {
    const id = handleCanvasMove(e);
    setHoveredId(id);
  }, [handleCanvasMove]);

  const onLeave = useCallback(() => {
    setHoveredId(null);
  }, []);

  return (
    <div className="pixel-scene-canvas">
      <canvas
        ref={canvasRef}
        width={CANVAS_W}
        height={CANVAS_H}
        className="pixel-scene-canvas__canvas"
        onClick={onClick}
        onMouseMove={onMove}
        onMouseLeave={onLeave}
      />
    </div>
  );
});

export default PixelSceneCanvas;
