/**
 * LocationRenderer — delegates all rendering to PixelSceneCanvas (Canvas 2D).
 *
 * This component is now a thin wrapper that maps the game's LocationConfig
 * and interaction props to the Canvas-based renderer. All actual drawing
 * happens in useCanvasRenderer → canvasDrawing.ts.
 *
 * ── COORDINATE SYSTEM ──
 * x, y: 0–100 (percentage of scene dimensions)
 * sceneHeight: 0–100 (percentage of scene height)
 * scale: multiplier on top of sceneHeight (default 1)
 *
 * ── RENDERING PIPELINE ──
 * LocationConfig → useCanvasRenderer → Canvas 2D drawImage()
 * No DOM elements for sprites. No CSS transforms. No background-position.
 */
import { memo, useState } from 'react';
import { PixelSceneCanvas } from './PixelSceneCanvas';
import type { LocationConfig } from '../../types/location.types';

export interface LocationRendererProps {
  readonly config: LocationConfig;
  readonly selectedObjectId?: string | null;
  readonly onObjectClick?: (objectId: string, actionCode: string) => void;
  readonly characterAnimations?: Record<string, string>;
  readonly timeOfDay?: string;
}

export const LocationRenderer = memo(function LocationRenderer(props: LocationRendererProps) {
  const [hoveredObjectId, setHoveredObjectId] = useState<string | null>(null);

  return (
    <PixelSceneCanvas
      config={props.config}
      timeOfDay={props.timeOfDay || 'day'}
      selectedObjectId={props.selectedObjectId || null}
      hoveredObjectId={hoveredObjectId}
      onObjectClick={(id) => {
        if (id && props.onObjectClick) {
          const furniture = props.config.furniture?.find((f: any) => f.id === id);
          if (furniture?.actionCode) {
            props.onObjectClick(id, furniture.actionCode);
          }
        }
      }}
      onObjectHover={setHoveredObjectId}
    />
  );
});

export default LocationRenderer;
