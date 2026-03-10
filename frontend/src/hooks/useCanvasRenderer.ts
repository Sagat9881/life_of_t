/**
 * useCanvasRenderer
 *
 * Renders a pixel-art scene on a <canvas> element.
 *
 * Atlas format (from LayeredAssetGenerator / sprite-atlas.json):
 *   layout: "strip"  — all frames in a single horizontal row
 *   columns          — number of frames
 *   fps              — playback speed
 *   cropOffset       — {x, y, originalWidth, originalHeight} of the
 *                      logical sprite within the full canvas size
 *   frameWidth/frameHeight — 0 means "derive from image / columns"
 *
 * URL convention (matches StaticResourceConfig.java):
 *   /assets/{type}/{name}/animations/{animation}_atlas.png
 *   /assets/{type}/{name}/sprite-atlas.json
 *
 * Rendering layers (bottom → top):
 *   1. Background  (single frame, stretched to canvas)
 *   2. Furniture   (static single frame, z-sorted)
 *   3. Characters  (animated strip, z-sorted)
 */

import { useEffect, useRef } from 'react';
import type { LocationConfig, FurniturePlacement, CharacterSlot } from '../types/location.types';

// ─────────────────────────────────────────────────────────────
// Types
// ─────────────────────────────────────────────────────────────

interface CropOffset {
  x: number;
  y: number;
  originalWidth: number;
  originalHeight: number;
}

interface AnimationConfig {
  file: string;
  layout: 'strip';
  columns: number;
  frameWidth: number;   // 0 = derive
  frameHeight: number;  // 0 = derive
  fps: number;
  loop: boolean;
  cropOffset: CropOffset;
}

interface AtlasConfig {
  entity: string;
  animations: Record<string, AnimationConfig>;
}

interface SlotState {
  animationName: string;
  frameIndex: number;
  lastFrameTime: number;
}

export interface UseCanvasRendererOptions {
  config: LocationConfig;
  canvasRef: React.RefObject<HTMLCanvasElement>;
  timeOfDay: string;
  selectedObjectId: string | null;
  hoveredObjectId: string | null;
  characterAnimations?: Record<string, string>;
}

// ─────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────

const atlasUrl = (type: string, name: string, animation: string): string =>
  `/assets/${type}/${name}/animations/${animation}_atlas.png`;

const atlasConfigUrl = (type: string, name: string): string =>
  `/assets/${type}/${name}/sprite-atlas.json`;

async function fetchAtlasConfig(type: string, name: string): Promise<AtlasConfig | null> {
  try {
    const res = await fetch(atlasConfigUrl(type, name));
    if (!res.ok) return null;
    return (await res.json()) as AtlasConfig;
  } catch {
    return null;
  }
}

function loadImage(src: string): Promise<HTMLImageElement | null> {
  return new Promise((resolve) => {
    const img = new Image();
    img.onload = () => resolve(img);
    img.onerror = () => {
      console.warn(`[useCanvasRenderer] 404: ${src}`);
      resolve(null);
    };
    img.src = src;
  });
}

/** Derive effective frameWidth from atlas image and column count. */
function frameW(img: HTMLImageElement, cfg: AnimationConfig): number {
  return cfg.frameWidth > 0 ? cfg.frameWidth : Math.floor(img.naturalWidth / cfg.columns);
}

/** Derive effective frameHeight from atlas image. */
function frameH(img: HTMLImageElement, cfg: AnimationConfig): number {
  return cfg.frameHeight > 0 ? cfg.frameHeight : img.naturalHeight;
}

// ─────────────────────────────────────────────────────────────
// Hook
// ─────────────────────────────────────────────────────────────

export function useCanvasRenderer({
  config,
  canvasRef,
  timeOfDay,
  selectedObjectId,
  hoveredObjectId,
  characterAnimations,
}: UseCanvasRendererOptions): void {
  // Loaded sprite images keyed by atlasUrl()
  const imagesRef = useRef<Map<string, HTMLImageElement>>(new Map());
  // Atlas JSON configs keyed by atlasConfigUrl()
  const atlasConfigsRef = useRef<Map<string, AtlasConfig>>(new Map());
  // Per-character-slot animation state
  const slotStateRef = useRef<Map<string, SlotState>>(new Map());
  // RAF handle
  const rafRef = useRef<number | undefined>(undefined);
  // Latest render-loop inputs as refs so RAF never needs to restart
  const selectedRef = useRef(selectedObjectId);
  const hoveredRef = useRef(hoveredObjectId);
  const charAnimsRef = useRef(characterAnimations);

  selectedRef.current = selectedObjectId;
  hoveredRef.current = hoveredObjectId;
  charAnimsRef.current = characterAnimations;

  // ── Effect 1: load assets when location config changes ─────
  useEffect(() => {
    // Clear cached assets when location changes
    imagesRef.current.clear();
    atlasConfigsRef.current.clear();
    slotStateRef.current.clear();

    let cancelled = false;

    const load = async (): Promise<void> => {
      const imagePromises: Promise<void>[] = [];
      const configPromises: Promise<void>[] = [];

      const scheduleImage = (type: string, name: string, animation: string): void => {
        const url = atlasUrl(type, name, animation);
        if (imagesRef.current.has(url)) return;
        imagePromises.push(
          loadImage(url).then((img) => {
            if (img && !cancelled) imagesRef.current.set(url, img);
          })
        );
      };

      const scheduleConfig = (type: string, name: string): void => {
        const key = atlasConfigUrl(type, name);
        if (atlasConfigsRef.current.has(key)) return;
        configPromises.push(
          fetchAtlasConfig(type, name).then((cfg) => {
            if (cfg && !cancelled) atlasConfigsRef.current.set(key, cfg);
          })
        );
      };

      // Background
      scheduleImage('locations', config.locationAsset, config.backgroundAnimation);
      scheduleConfig('locations', config.locationAsset);

      // Furniture
      config.furniture.forEach((f: FurniturePlacement) => {
        scheduleImage('furniture', f.entityName, f.animation);
        scheduleConfig('furniture', f.entityName);
      });

      // Characters
      config.characters.forEach((c: CharacterSlot) => {
        const anim = charAnimsRef.current?.[c.id] ?? c.defaultAnimation;
        scheduleImage('characters', c.entityName, anim);
        scheduleConfig('characters', c.entityName);
        // Pre-init slot state
        if (!slotStateRef.current.has(c.id)) {
          slotStateRef.current.set(c.id, {
            animationName: anim,
            frameIndex: 0,
            lastFrameTime: 0,
          });
        }
      });

      await Promise.all([...imagePromises, ...configPromises]);
    };

    load().catch((err) => console.error('[useCanvasRenderer] load error:', err));

    return () => { cancelled = true; };
  }, [config]); // only reload when location changes

  // ── Effect 2: RAF render loop — never restarts ──────────────
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const W = canvas.width;   // 640
    const H = canvas.height;  // 480

    const drawSprite = (
      img: HTMLImageElement,
      animCfg: AnimationConfig | undefined,
      slotId: string | null,
      destX: number,
      destY: number,
      destH: number,
      now: number
    ): void => {
      // No atlas config → draw full image (fallback for backgrounds/static)
      if (!animCfg || animCfg.columns <= 1) {
        const destW = img.naturalWidth > 0
          ? destH * (img.naturalWidth / img.naturalHeight)
          : destH;
        ctx.drawImage(img, destX - destW / 2, destY - destH, destW, destH);
        return;
      }

      const fw = frameW(img, animCfg);
      const fh = frameH(img, animCfg);
      const destW = fh > 0 ? destH * (fw / fh) : destH;

      // Advance frame for character slots
      let frame = 0;
      if (slotId !== null) {
        let state = slotStateRef.current.get(slotId);
        const currentAnim = charAnimsRef.current?.[slotId];

        // Reset frame if animation changed
        if (state && currentAnim && state.animationName !== currentAnim) {
          state = { animationName: currentAnim, frameIndex: 0, lastFrameTime: now };
          slotStateRef.current.set(slotId, state);
        }

        if (state) {
          const frameDuration = 1000 / animCfg.fps;
          if (now - state.lastFrameTime >= frameDuration) {
            let next = state.frameIndex + 1;
            if (next >= animCfg.columns) next = animCfg.loop ? 0 : animCfg.columns - 1;
            state.frameIndex = next;
            state.lastFrameTime = now;
          }
          frame = state.frameIndex;
        }
      }

      // Source rect: strip layout, frame index = column
      const sx = frame * fw;
      const sy = 0;

      ctx.drawImage(
        img,
        sx, sy, fw, fh,                        // source
        destX - destW / 2, destY - destH, destW, destH  // dest
      );
    };

    const render = (now: number): void => {
      ctx.clearRect(0, 0, W, H);
      ctx.imageSmoothingEnabled = false; // keep pixel-art crisp

      const cfg = config;
      const selected = selectedRef.current;
      const hovered = hoveredRef.current;

      // 1. Background — single frame, stretched
      const bgUrl = atlasUrl('locations', cfg.locationAsset, cfg.backgroundAnimation);
      const bgImg = imagesRef.current.get(bgUrl);
      if (bgImg) {
        ctx.drawImage(bgImg, 0, 0, W, H);
      } else {
        // Fallback: dark grey placeholder
        ctx.fillStyle = '#1a1a2e';
        ctx.fillRect(0, 0, W, H);
      }

      // 2. Furniture — static single frame, z-sorted
      const sortedFurniture = [...cfg.furniture].sort(
        (a, b) => a.zOrder - b.zOrder
      );
      for (const f of sortedFurniture) {
        const url = atlasUrl('furniture', f.entityName, f.animation);
        const img = imagesRef.current.get(url);
        if (!img) continue;

        const atlasKey = atlasConfigUrl('furniture', f.entityName);
        const atlasCfg = atlasConfigsRef.current.get(atlasKey);
        const animCfg = atlasCfg?.animations[f.animation];

        ctx.save();
        if (f.id === selected) {
          ctx.shadowColor = '#4a90e2';
          ctx.shadowBlur = 12;
        } else if (f.id === hovered) {
          ctx.shadowColor = '#ffffff';
          ctx.shadowBlur = 6;
        }

        const x = (f.x / 100) * W;
        const y = (f.y / 100) * H;
        const h = (f.sceneHeight / 100) * H * f.scale;
        drawSprite(img, animCfg, null, x, y, h, now);
        ctx.restore();
      }

      // 3. Characters — animated, z-sorted
      const sortedChars = [...cfg.characters].sort(
        (a, b) => a.zOrder - b.zOrder
      );
      for (const c of sortedChars) {
        const animName = charAnimsRef.current?.[c.id] ?? c.defaultAnimation;
        const url = atlasUrl('characters', c.entityName, animName);
        const img = imagesRef.current.get(url);
        if (!img) continue;

        const atlasKey = atlasConfigUrl('characters', c.entityName);
        const atlasCfg = atlasConfigsRef.current.get(atlasKey);
        const animCfg = atlasCfg?.animations[animName];

        const x = (c.x / 100) * W;
        const y = (c.y / 100) * H;
        const h = (c.sceneHeight / 100) * H * c.scale;
        drawSprite(img, animCfg, c.id, x, y, h, now);
      }

      rafRef.current = requestAnimationFrame(render);
    };

    rafRef.current = requestAnimationFrame(render);

    return () => {
      if (rafRef.current !== undefined) cancelAnimationFrame(rafRef.current);
    };
  }, [canvasRef]); // RAF loop never restarts — reads everything via refs
}
