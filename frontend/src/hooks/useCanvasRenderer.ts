/**
 * useCanvasRenderer — thin orchestration shell.
 *
 * Public API is identical to the original monolithic hook.
 * All asset loading is delegated to useCanvasAssets.
 * All rendering is delegated to useCanvasRenderLoop.
 */

import { useRef } from 'react';
import type { LocationConfig } from '../types/location.types';
import type { AtlasConfig, SlotState } from './canvasTypes';
import { useCanvasAssets } from './useCanvasAssets';
import { useCanvasRenderLoop } from './useCanvasRenderLoop';

export interface UseCanvasRendererOptions {
  config: LocationConfig;
  canvasRef: React.RefObject<HTMLCanvasElement>;
  viewportRef: React.RefObject<{ vpX: number; vpY: number; vpW: number; vpH: number }>;
  timeOfDay?: string;
  selectedObjectId: string | null;
  hoveredObjectId: string | null;
  characterAnimations?: Record<string, string>;
}

export function useCanvasRenderer({
  config,
  canvasRef,
  viewportRef,
  selectedObjectId,
  hoveredObjectId,
  characterAnimations,
}: UseCanvasRendererOptions): void {

  const imagesRef       = useRef<Map<string, HTMLImageElement>>(new Map());
  const atlasConfigsRef = useRef<Map<string, AtlasConfig>>(new Map());
  const slotStateRef    = useRef<Map<string, SlotState>>(new Map());
  const configRef       = useRef<LocationConfig>(config);
  const selectedRef     = useRef<string | null>(selectedObjectId);
  const hoveredRef      = useRef<string | null>(hoveredObjectId);
  const charAnimsRef    = useRef<Record<string, string> | undefined>(characterAnimations);
  const rafRef          = useRef<number | undefined>(undefined);

  // Keep refs in sync on every render
  configRef.current    = config;
  selectedRef.current  = selectedObjectId;
  hoveredRef.current   = hoveredObjectId;
  charAnimsRef.current = characterAnimations;

  useCanvasAssets({
    config,
    characterAnimations,
    assetsRefs: { imagesRef, atlasConfigsRef, slotStateRef },
  });

  useCanvasRenderLoop({
    canvasRef,
    viewportRef,
    assetsRefs: { imagesRef, atlasConfigsRef, slotStateRef },
    configRef,
    selectedRef,
    hoveredRef,
    charAnimsRef,
    rafRef,
  });
}
