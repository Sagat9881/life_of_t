/**
 * PixelScene — container for pixel-art game scenes.
 *
 * Provides a fixed-aspect-ratio viewport with layered rendering zones:
 * - background: location background (z: 0-9)
 * - midground: furniture and objects (z: 10-49)
 * - foreground: characters and effects (z: 50+)
 *
 * All child elements are positioned absolutely within the scene using % coordinates.
 * The scene scales uniformly via CSS to fit the parent while preserving pixel-art crispness.
 */
import { type ReactNode, memo } from 'react';
import './PixelScene.css';

export interface PixelSceneProps {
  /** Scene width in logical pixels (default: 320 — classic pixel game width) */
  readonly width?: number;
  /** Scene height in logical pixels (default: 180 — 16:9 ratio) */
  readonly height?: number;
  /** CSS scale multiplier for the scene (default: auto-fit parent) */
  readonly className?: string;
  readonly children?: ReactNode;
}

export const PixelScene = memo(function PixelScene({
  width = 320,
  height = 180,
  className,
  children,
}: PixelSceneProps) {
  return (
    <div
      className={`pixel-scene ${className ?? ''}`}
      style={{
        aspectRatio: `${width} / ${height}`,
      }}
    >
      <div className="pixel-scene__viewport">
        {children}
      </div>
    </div>
  );
});

export default PixelScene;
