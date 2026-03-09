/**
 * useCanvasRenderer — central render loop for Canvas 2D scene.
 *
 * Loads all atlas configs + images for a LocationConfig,
 * manages per-entity animation frame state, and draws all layers
 * each requestAnimationFrame tick.
 *
 * Layers (back to front):
 *   0. Background (location atlas)
 *   1. Furniture (sorted by zOrder)
 *   2. Characters + pets (sorted by zOrder)
 *   3. Ambient overlay (time-of-day tint)
 *   4. UI overlays (selection glow, labels)
 */
import { useCallback, useEffect, useRef, useState } from 'react';
import { loadAtlasConfig, resolveAnimation, preloadAtlasImage } from '@/services/assetService';
import type { SpriteAnimation, AtlasConfig } from '@/types/sprite';
import {
  SCENE_W, SCENE_H,
  drawBackground, drawSpriteFrame, drawAmbientOverlay,
  drawSelectionGlow, drawLabel,
  type HitBox, hitTest,
} from '@/utils/canvasDrawing';
import type { LocationConfig, FurniturePlacement, CharacterSlot } from '@/config/locations';

/* ── Types ── */

interface EntityAnimState {
  anim: SpriteAnimation;
  img: HTMLImageElement;
  frame: number;
  lastFrameTime: number;
}

interface SceneState {
  bg: EntityAnimState | null;
  furniture: Map<string, EntityAnimState>;
  characters: Map<string, EntityAnimState>;
  hitBoxes: HitBox[];
}

export interface UseCanvasRendererOptions {
  config: LocationConfig;
  canvasRef: React.RefObject<HTMLCanvasElement | null>;
  timeOfDay?: string;
  selectedObjectId?: string | null;
  hoveredObjectId?: string | null;
  characterAnimations?: Record<string, string>;
}

const TIME_MAP: Record<string, string> = {
  MORNING: 'morning', DAY: 'day', EVENING: 'evening', NIGHT: 'night',
  morning: 'morning', day: 'day', evening: 'evening', night: 'night',
};

/* ── Hook ── */

export function useCanvasRenderer({
  config,
  canvasRef,
  timeOfDay = 'day',
  selectedObjectId,
  hoveredObjectId,
  characterAnimations,
}: UseCanvasRendererOptions) {
  const sceneRef = useRef<SceneState>({
    bg: null,
    furniture: new Map(),
    characters: new Map(),
    hitBoxes: [],
  });
  const rafRef = useRef<number>(0);
  const [isLoaded, setIsLoaded] = useState(false);
  const [hitBoxes, setHitBoxes] = useState<HitBox[]>([]);

  const condition = TIME_MAP[timeOfDay] ?? 'day';

  /* ── Load all assets ── */
  useEffect(() => {
    let cancelled = false;

    const loadEntity = async (
      entityType: string,
      entityName: string,
      animationName: string
    ): Promise<EntityAnimState | null> => {
      try {
        const atlasConfig = await loadAtlasConfig(entityType, entityName);
        const anim = resolveAnimation(entityType, entityName, animationName, atlasConfig, condition);
        if (!anim) return null;
        const img = await preloadAtlasImage(anim.atlasUrl);
        return { anim, img, frame: 0, lastFrameTime: 0 };
      } catch {
        return null;
      }
    };

    const loadAll = async () => {
      // Background
      const bg = await loadEntity('locations', config.locationAsset, config.backgroundAnimation);

      // Furniture
      const furnitureMap = new Map<string, EntityAnimState>();
      await Promise.allSettled(
        config.furniture.map(async (f) => {
          const state = await loadEntity('furniture', f.entityName, f.animation);
          if (state) furnitureMap.set(f.id, state);
        })
      );

      // Characters
      const charMap = new Map<string, EntityAnimState>();
      await Promise.allSettled(
        config.characters.map(async (c) => {
          const animName = characterAnimations?.[c.entityName] ?? c.defaultAnimation;
          const state = await loadEntity('characters', c.entityName, animName);
          if (state) charMap.set(c.id, state);
        })
      );

      if (cancelled) return;

      // Build hit boxes for interactive furniture
      const boxes: HitBox[] = [];
      config.furniture.forEach((f) => {
        if (!f.actionCode) return;
        const state = furnitureMap.get(f.id);
        if (!state) return;
        const h = (f.sceneHeight / 100) * SCENE_H * (f.scale ?? 1);
        const w = h * (state.anim.frameWidth / state.anim.frameHeight);
        boxes.push({
          id: f.id,
          actionCode: f.actionCode,
          label: f.label,
          x: (f.x / 100) * SCENE_W,
          y: (f.y / 100) * SCENE_H,
          width: w,
          height: h,
          zOrder: f.zOrder,
        });
      });

      sceneRef.current = { bg, furniture: furnitureMap, characters: charMap, hitBoxes: boxes };
      setHitBoxes(boxes);
      setIsLoaded(true);
    };

    void loadAll();
    return () => { cancelled = true; };
  }, [config, condition, characterAnimations]);

  /* ── Render loop ── */
  const render = useCallback((timestamp: number) => {
    const canvas = canvasRef.current;
    if (!canvas) { rafRef.current = requestAnimationFrame(render); return; }

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const scene = sceneRef.current;

    // Setup
    ctx.imageSmoothingEnabled = false;
    ctx.clearRect(0, 0, SCENE_W, SCENE_H);

    // Layer 0: Background
    if (scene.bg) {
      advanceFrame(scene.bg, timestamp);
      drawBackground(ctx, scene.bg.img, scene.bg.anim, scene.bg.frame);
    }

    // Layer 1: Furniture (sorted by zOrder)
    const sortedFurniture = [...config.furniture].sort((a, b) => a.zOrder - b.zOrder);
    for (const f of sortedFurniture) {
      const state = scene.furniture.get(f.id);
      if (!state) continue;
      advanceFrame(state, timestamp);

      const sceneX = (f.x / 100) * SCENE_W;
      const sceneY = (f.y / 100) * SCENE_H;
      const h = (f.sceneHeight / 100) * SCENE_H;

      // Selection/hover glow
      if (f.actionCode && (selectedObjectId === f.id || hoveredObjectId === f.id)) {
        const w = h * (state.anim.frameWidth / state.anim.frameHeight);
        drawSelectionGlow(ctx, sceneX, sceneY, w * (f.scale ?? 1), h * (f.scale ?? 1));
      }

      drawSpriteFrame(ctx, state.img, state.anim, state.frame, sceneX, sceneY, h, f.scale ?? 1);

      // Label on hover
      if (f.label && (hoveredObjectId === f.id || selectedObjectId === f.id)) {
        drawLabel(ctx, f.label, sceneX, sceneY, h * (f.scale ?? 1));
      }
    }

    // Layer 2: Characters (sorted by zOrder)
    const sortedChars = [...config.characters].sort((a, b) => a.zOrder - b.zOrder);
    for (const c of sortedChars) {
      const state = scene.characters.get(c.id);
      if (!state) continue;
      advanceFrame(state, timestamp);

      const sceneX = (c.x / 100) * SCENE_W;
      const sceneY = (c.y / 100) * SCENE_H;
      // Characters: sceneHeight from config (percentage of scene)
      const h = ((c as CharacterSlotWithHeight).sceneHeight ?? 38) / 100 * SCENE_H;

      drawSpriteFrame(ctx, state.img, state.anim, state.frame, sceneX, sceneY, h, c.scale ?? 1);
    }

    // Layer 3: Ambient overlay
    drawAmbientOverlay(ctx, condition);

    rafRef.current = requestAnimationFrame(render);
  }, [canvasRef, config, condition, selectedObjectId, hoveredObjectId]);

  /* ── Start/stop render loop ── */
  useEffect(() => {
    if (!isLoaded) return;
    rafRef.current = requestAnimationFrame(render);
    return () => {
      if (rafRef.current) cancelAnimationFrame(rafRef.current);
    };
  }, [isLoaded, render]);

  /* ── Click handler ── */
  const handleCanvasClick = useCallback((e: React.MouseEvent<HTMLCanvasElement>) => {
    const canvas = canvasRef.current;
    if (!canvas) return null;

    const rect = canvas.getBoundingClientRect();
    const scaleX = SCENE_W / rect.width;
    const scaleY = SCENE_H / rect.height;
    const sceneX = (e.clientX - rect.left) * scaleX;
    const sceneY = (e.clientY - rect.top) * scaleY;

    return hitTest(sceneX, sceneY, sceneRef.current.hitBoxes);
  }, [canvasRef]);

  /* ── Hover handler ── */
  const handleCanvasMove = useCallback((e: React.MouseEvent<HTMLCanvasElement>): string | null => {
    const canvas = canvasRef.current;
    if (!canvas) return null;

    const rect = canvas.getBoundingClientRect();
    const scaleX = SCENE_W / rect.width;
    const scaleY = SCENE_H / rect.height;
    const sceneX = (e.clientX - rect.left) * scaleX;
    const sceneY = (e.clientY - rect.top) * scaleY;

    const hit = hitTest(sceneX, sceneY, sceneRef.current.hitBoxes);
    canvas.style.cursor = hit?.actionCode ? 'pointer' : 'default';
    return hit?.id ?? null;
  }, [canvasRef]);

  return { isLoaded, hitBoxes, handleCanvasClick, handleCanvasMove };
}

/* ── Helpers ── */

function advanceFrame(state: EntityAnimState, timestamp: number): void {
  const frameDuration = 1000 / state.anim.fps;
  if (timestamp - state.lastFrameTime >= frameDuration) {
    state.lastFrameTime = timestamp;
    const next = state.frame + 1;
    if (next >= state.anim.frameCount) {
      state.frame = state.anim.loop ? 0 : state.anim.frameCount - 1;
    } else {
      state.frame = next;
    }
  }
}

/** Extended character slot with optional sceneHeight */
interface CharacterSlotWithHeight extends CharacterSlot {
  sceneHeight?: number;
}
