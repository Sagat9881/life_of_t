/**
 * useCanvasRenderer — thin orchestration shell.
 *
 * Public API is identical to the original monolithic hook.
 * All asset loading is delegated to useCanvasAssets.
 * All rendering is delegated to useCanvasRenderLoop.
 */

import { useRef } from 'react';
import type { LocationConfig } from '../types/location.types';
import type { AtlasConfig, GameStateSnapshot, SlotState } from './canvasTypes';
import { useCanvasAssets } from './useCanvasAssets';
import { useCanvasRenderLoop } from './useCanvasRenderLoop';

export interface UseCanvasRendererOptions {
  config: LocationConfig;
  canvasRef: React.RefObject<HTMLCanvasElement>;
  viewportRef: React.RefObject<{ vpX: number; vpY: number; vpW: number; vpH: number }>;
  timeOfDay?: string;
  gameState?: GameStateSnapshot;
  selectedObjectId: string | null;
  hoveredObjectId: string | null;
  characterAnimations?: Record<string, string>;
}

const EMPTY_GAME_STATE: GameStateSnapshot = {
  player: { energy: 0, mood: 0, hunger: 0, health: 0, money: 0, location: 'home' },
  context: { time: 'morning', day: 1, hour: 7, timeSlot: 'MORNING' },
  npc: {},
};

export function useCanvasRenderer({
  config,
  canvasRef,
  viewportRef,
  timeOfDay,
  gameState,
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
  const gameStateRef    = useRef<GameStateSnapshot>(gameState ?? EMPTY_GAME_STATE);

  // Keep refs in sync on every render
  configRef.current    = config;
  selectedRef.current  = selectedObjectId;
  hoveredRef.current   = hoveredObjectId;
  charAnimsRef.current = characterAnimations;
  gameStateRef.current = gameState ?? EMPTY_GAME_STATE;

  useCanvasAssets({
    config,
    characterAnimations: characterAnimations ?? {},
    timeOfDay,
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
    gameStateRef,
  });
}
