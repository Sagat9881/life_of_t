/**
 * PixelScene — container for pixel-art game scenes.
 *
 * Renders at native scene resolution (SCENE_WIDTH × SCENE_HEIGHT) and
 * auto-scales the entire scene to fit the parent container via CSS transform.
 *
 * ALL children are rendered inside the native-resolution viewport.
 * Coordinates and sizes stay in "scene-logical pixels" and get
 * uniformly upscaled by a single CSS scale() transform.
 *
 * The background (location) fills this viewport exactly.
 * Characters, furniture — all sized relative to this viewport.
 */
import { type ReactNode, memo, useRef, useState, useEffect, useCallback } from 'react';
import { SCENE_WIDTH, SCENE_HEIGHT } from '@/utils/sceneConstants';
import './PixelScene.css';

export interface PixelSceneProps {
  /** Override scene width (default: SCENE_WIDTH=640) */
  readonly width?: number;
  /** Override scene height (default: SCENE_HEIGHT=480) */
  readonly height?: number;
  readonly className?: string;
  readonly children?: ReactNode;
}

export const PixelScene = memo(function PixelScene({
  width = SCENE_WIDTH,
  height = SCENE_HEIGHT,
  className,
  children,
}: PixelSceneProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const [scale, setScale] = useState(1);

  const updateScale = useCallback(() => {
    const el = containerRef.current;
    if (!el) return;
    const parentW = el.clientWidth;
    const parentH = el.clientHeight;
    const scaleX = parentW / width;
    const scaleY = parentH / height;
    setScale(Math.min(scaleX, scaleY));
  }, [width, height]);

  useEffect(() => {
    updateScale();
    const observer = new ResizeObserver(updateScale);
    if (containerRef.current) {
      observer.observe(containerRef.current);
    }
    return () => observer.disconnect();
  }, [updateScale]);

  return (
    <div
      ref={containerRef}
      className={`pixel-scene ${className ?? ''}`}
      style={{
        aspectRatio: `${width} / ${height}`,
      }}
    >
      <div
        className="pixel-scene__viewport"
        style={{
          width: `${width}px`,
          height: `${height}px`,
          transform: `scale(${scale})`,
          transformOrigin: 'top left',
        }}
      >
        {children}
      </div>
    </div>
  );
});

export default PixelScene;
