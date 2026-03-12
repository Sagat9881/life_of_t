/**
 * Shared types for Canvas pipeline hooks.
 * Extracted from the original useCanvasRenderer monolith.
 * Do NOT import from this file in non-canvas code.
 */

export interface CropOffset {
  x: number;
  y: number;
  originalWidth: number;
  originalHeight: number;
}

export interface AnimationConfig {
  file: string;
  layout: 'strip';
  columns: number;
  frameWidth: number;
  frameHeight: number;
  fps: number;
  loop: boolean;
  cropOffset?: CropOffset;
}

export interface AtlasConfig {
  entity: string;
  animations: Record<string, AnimationConfig>;
}

export interface SlotState {
  animationName: string;
  frameIndex: number;
  lastFrameTime: number;
}
