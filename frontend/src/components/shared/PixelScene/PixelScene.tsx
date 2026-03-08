/**
 * PixelScene — container for pixel-art game scenes.
 *
 * Renders at a native pixel resolution (e.g. 640×480) and auto-scales
 * the entire scene to fit the parent container. All children are rendered
 * inside the native-resolution viewport, so coordinates/sizes stay in
 * "native pixels" and get uniformly upscaled by CSS transform.
 *
 * Default resolution is 640×480 (4:3) which provides enough space for
 * HD pixel art assets (128×192 characters, 640×480 locations).
 */
import { type ReactNode, memo, useRef, useState, useEffect, useCallback } from 'react';
import './PixelScene.css';

export interface PixelSceneProps {
  /** Scene width in logical pixels (native resolution) */
  readonly width?: number;
  /** Scene height in logical pixels (native resolution) */
  readonly height?: number;
  readonly className?: string;
  readonly children?: ReactNode;
}

export const PixelScene = memo(function PixelScene({
  width = 640,
  height = 480,
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
    // Fit inside parent while preserving aspect ratio
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
