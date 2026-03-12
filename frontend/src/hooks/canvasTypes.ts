/**
 * Shared types for Canvas pipeline hooks.
 * Extracted from the original useCanvasRenderer monolith.
 * Do NOT import from this file in non-canvas code.
 */

import React from 'react';

export interface CropOffset {
  x: number;
  y: number;
  originalWidth: number;
  originalHeight: number;
}

export interface AnimationConfig {
  file: string;
  layout: 'strip' | 'grid';
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

export interface CanvasAssetsRefs {
  imagesRef: React.MutableRefObject<Map<string, HTMLImageElement>>;
  atlasConfigsRef: React.MutableRefObject<Map<string, AtlasConfig>>;
  slotStateRef: React.MutableRefObject<Map<string, SlotState>>;
}
