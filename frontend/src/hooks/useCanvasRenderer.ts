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
 *   1. Background  (animated strip, stretched to canvas)
 *   2. Furniture   (static single frame, z-sorted)
 *   3. Characters  (animated strip, z-sorted)
 *
 * Effect structure (3 effects, no restarts):
 *   Effect 1 — dep: [config]              — full asset reload on location change
 *   Effect 2 — dep: [characterAnimations] — delta-load new anim sprites
 *   Effect 3 — dep: [canvasRef]           — RAF loop, never restarts
 */

import { useEffect, useRef } from 'react';
import type { LocationConfig, FurniturePlacement, CharacterSlot } from '../types/location.types';

// ─────────────────────────────────────────────────────────────────────────────
// Types
// ─────────────────────────────────────────────────────────────────────────────

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
  frameWidth: number;   // 0 = derive from image
  frameHeight: number;  // 0 = derive from image
  fps: number;
  loop: boolean;
  cropOffset?: CropOffset; // optional — not all atlases carry crop data
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
  timeOfDay?: string; // kept in interface for callers, not used internally
  selectedObjectId: string | null;
  hoveredObjectId: string | null;
  characterAnimations?: Record<string, string>;
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

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
    img.onload  = () => resolve(img);
    img.onerror = () => { console.warn(`[canvas] 404: ${src}`); resolve(null); };
    img.src = src;
  });
}

function frameW(img: HTMLImageElement, cfg: AnimationConfig): number {
  return cfg.frameWidth > 0 ? cfg.frameWidth : Math.floor(img.naturalWidth / cfg.columns);
}

function frameH(img: HTMLImageElement, cfg: AnimationConfig): number {
  return cfg.frameHeight > 0 ? cfg.frameHeight : img.naturalHeight;
}

// ─────────────────────────────────────────────────────────────────────────────
// Hook
// ─────────────────────────────────────────────────────────────────────────────

export function useCanvasRenderer({
  config,
  canvasRef,
  selectedObjectId,
  hoveredObjectId,
  characterAnimations,
}: UseCanvasRendererOptions): void {

  // Stable refs — RAF reads these without restarting
  const imagesRef       = useRef<Map<string, HTMLImageElement>>(new Map());
  const atlasConfigsRef = useRef<Map<string, AtlasConfig>>(new Map());
  const slotStateRef    = useRef<Map<string, SlotState>>(new Map());
  const configRef       = useRef(config);
  const selectedRef     = useRef(selectedObjectId);
  const hoveredRef      = useRef(hoveredObjectId);
  const charAnimsRef    = useRef(characterAnimations);
  const rafRef          = useRef<number | undefined>(undefined);

  // Keep refs in sync on every render
  configRef.current    = config;
  selectedRef.current  = selectedObjectId;
  hoveredRef.current   = hoveredObjectId;
  charAnimsRef.current = characterAnimations;

  // ── Effect 1: Full asset reload when location config changes ───────────────
  useEffect(() => {
    imagesRef.current.clear();
    atlasConfigsRef.current.clear();
    slotStateRef.current.clear();

    let cancelled = false;

    const loadAll = async (): Promise<void> => {
      const work: Promise<void>[] = [];

      const enqueueImage = (type: string, name: string, anim: string): void => {
        const url = atlasUrl(type, name, anim);
        if (imagesRef.current.has(url)) return;
        work.push(
          loadImage(url).then((img) => {
            if (img && !cancelled) imagesRef.current.set(url, img);
          })
        );
      };

      const enqueueConfig = (type: string, name: string): void => {
        const key = atlasConfigUrl(type, name);
        if (atlasConfigsRef.current.has(key)) return;
        work.push(
          fetchAtlasConfig(type, name).then((cfg) => {
            if (cfg && !cancelled) atlasConfigsRef.current.set(key, cfg);
          })
        );
      };

      enqueueImage('locations', config.locationAsset, config.backgroundAnimation);
      enqueueConfig('locations', config.locationAsset);

      // Initialize SlotState for background
      slotStateRef.current.set('__background__', { animationName: '__bg__', frameIndex: 0, lastFrameTime: 0 });

      config.furniture.forEach((f: FurniturePlacement) => {
        enqueueImage('furniture', f.entityName, f.animation);
        enqueueConfig('furniture', f.entityName);
      });

      config.characters.forEach((c: CharacterSlot) => {
        const anim = charAnimsRef.current?.[c.id] ?? c.defaultAnimation;
        enqueueImage('characters', c.entityName, anim);
        enqueueConfig('characters', c.entityName);
        slotStateRef.current.set(c.id, { animationName: anim, frameIndex: 0, lastFrameTime: 0 });
      });

      await Promise.all(work);
    };

    loadAll().catch((e) => console.error('[canvas] load error:', e));
    return () => { cancelled = true; };
  }, [config]);

  // ── Effect 2: Delta-load when a character switches animation ──────────────
  useEffect(() => {
    if (!characterAnimations) return;
    let cancelled = false;

    const work: Promise<void>[] = [];
    config.characters.forEach((c: CharacterSlot) => {
      const anim = characterAnimations[c.id] ?? c.defaultAnimation;
      const url  = atlasUrl('characters', c.entityName, anim);
      if (imagesRef.current.has(url)) return;
      work.push(
        loadImage(url).then((img) => {
          if (img && !cancelled) imagesRef.current.set(url, img);
        })
      );
    });

    if (work.length > 0) {
      Promise.all(work).catch((e) => console.error('[canvas] delta load error:', e));
    }
    return () => { cancelled = true; };
  }, [characterAnimations, config.characters]);

  // ── Effect 3: RAF render loop — starts once, never restarts ───────────────
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
      now: number,
      flipX = false
    ): void => {
      if (!animCfg || animCfg.columns <= 1) {
        const fw = animCfg ? frameW(img, animCfg) : img.naturalWidth;
        const fh = animCfg ? frameH(img, animCfg) : img.naturalHeight;

        let drawX: number;
        let drawY: number;
        let drawW: number;
        let drawH: number;

        if (animCfg?.cropOffset) {
          const crop = animCfg.cropOffset;
          const scaleFactor = destH / crop.originalHeight;
          const fullW = crop.originalWidth * scaleFactor;
          const fullX = destX - fullW / 2;
          const fullY = destY - destH;
          drawX = fullX + crop.x * scaleFactor;
          drawY = fullY + crop.y * scaleFactor;
          drawW = fw * scaleFactor;
          drawH = fh * scaleFactor;
        } else {
          const dw = img.naturalWidth > 0 ? destH * (img.naturalWidth / img.naturalHeight) : destH;
          drawX = destX - dw / 2;
          drawY = destY - destH;
          drawW = dw;
          drawH = destH;
        }

        if (flipX) {
          ctx.save();
          ctx.translate(destX, 0);
          ctx.scale(-1, 1);
          ctx.translate(-destX, 0);
        }
        ctx.drawImage(img, 0, 0, fw, fh, drawX, drawY, drawW, drawH);
        if (flipX) {
          ctx.restore();
        }
        return;
      }

      const fw = frameW(img, animCfg);
      const fh = frameH(img, animCfg);

      let frame = 0;
      if (slotId !== null) {
        let state = slotStateRef.current.get(slotId);
        const currentAnim = charAnimsRef.current?.[slotId];

        if (state && currentAnim && state.animationName !== currentAnim) {
          state = { animationName: currentAnim, frameIndex: 0, lastFrameTime: now };
          slotStateRef.current.set(slotId, state);
        }

        if (state) {
          const interval = 1000 / animCfg.fps;
          if (now - state.lastFrameTime >= interval) {
            let next = state.frameIndex + 1;
            if (next >= animCfg.columns) next = animCfg.loop ? 0 : animCfg.columns - 1;
            state.frameIndex    = next;
            state.lastFrameTime = now;
          }
          frame = state.frameIndex;
        }
      }

      let drawX: number;
      let drawY: number;
      let drawW: number;
      let drawH: number;

      if (animCfg.cropOffset) {
        const crop = animCfg.cropOffset;
        const scaleFactor = destH / crop.originalHeight;
        const fullW = crop.originalWidth * scaleFactor;
        const fullX = destX - fullW / 2;
        const fullY = destY - destH;
        drawX = fullX + crop.x * scaleFactor;
        drawY = fullY + crop.y * scaleFactor;
        drawW = fw * scaleFactor;
        drawH = fh * scaleFactor;
      } else {
        const dw = fh > 0 ? destH * (fw / fh) : destH;
        drawX = destX - dw / 2;
        drawY = destY - destH;
        drawW = dw;
        drawH = destH;
      }

      if (flipX) {
        ctx.save();
        ctx.translate(destX, 0);
        ctx.scale(-1, 1);
        ctx.translate(-destX, 0);
      }
      ctx.drawImage(
        img,
        frame * fw, 0, fw, fh,
        drawX, drawY, drawW, drawH
      );
      if (flipX) {
        ctx.restore();
      }
    };

    const render = (now: number): void => {
      ctx.clearRect(0, 0, W, H);
      ctx.imageSmoothingEnabled = false;

      const cfg      = configRef.current;
      const selected = selectedRef.current;
      const hovered  = hoveredRef.current;

      // 1. Background — animated strip
      const bgUrl = atlasUrl('locations', cfg.locationAsset, cfg.backgroundAnimation);
      const bgImg = imagesRef.current.get(bgUrl);

      const drawBackground = (img: HTMLImageElement, animCfg: AnimationConfig | undefined, now: number): void => {
        const fw = animCfg ? frameW(img, animCfg) : img.naturalWidth;
        const fh = animCfg ? frameH(img, animCfg) : img.naturalHeight;

        let state = slotStateRef.current.get('__background__');
        if (!state) {
          state = { animationName: '__bg__', frameIndex: 0, lastFrameTime: now };
          slotStateRef.current.set('__background__', state);
        }

        const interval = 1000 / (animCfg?.fps ?? 1);
        if (now - state.lastFrameTime >= interval) {
          let next = state.frameIndex + 1;
          const cols = animCfg?.columns ?? 1;
          if (next >= cols) next = animCfg?.loop !== false ? 0 : cols - 1;
          state.frameIndex = next;
          state.lastFrameTime = now;
        }

        if (!animCfg || animCfg.columns <= 1) {
          ctx.drawImage(img, 0, 0, W, H);
          return;
        }

        ctx.drawImage(img, state.frameIndex * fw, 0, fw, fh, 0, 0, W, H);
      };

      if (bgImg) {
        const bgAtlasKey = atlasConfigUrl('locations', cfg.locationAsset);
        const bgAnimCfg = atlasConfigsRef.current.get(bgAtlasKey)?.animations[cfg.backgroundAnimation];
        drawBackground(bgImg, bgAnimCfg, now);
      } else {
        ctx.fillStyle = '#1a1a2e';
        ctx.fillRect(0, 0, W, H);
      }

      // 2. Furniture — static, z-sorted
      const furniture = [...cfg.furniture].sort((a, b) => a.zOrder - b.zOrder);
      for (const f of furniture) {
        const url = atlasUrl('furniture', f.entityName, f.animation);
        const img = imagesRef.current.get(url);
        if (!img) continue;

        const atlasKey = atlasConfigUrl('furniture', f.entityName);
        const animCfg  = atlasConfigsRef.current.get(atlasKey)?.animations[f.animation];

        ctx.save();
        if      (f.id === selected) { ctx.shadowColor = '#4a90e2'; ctx.shadowBlur = 12; }
        else if (f.id === hovered)  { ctx.shadowColor = '#ffffff'; ctx.shadowBlur = 6;  }

        drawSprite(img, animCfg, null,
          (f.x / 100) * W, (f.y / 100) * H,
          (f.sceneHeight / 100) * H * f.scale, now, f.flipX ?? false);
        ctx.restore();
      }

      // 3. Characters — animated, z-sorted
      const characters = [...cfg.characters].sort((a, b) => a.zOrder - b.zOrder);
      for (const c of characters) {
        const animName = charAnimsRef.current?.[c.id] ?? c.defaultAnimation;
        const url      = atlasUrl('characters', c.entityName, animName);
        const img      = imagesRef.current.get(url);
        if (!img) continue;

        const atlasKey = atlasConfigUrl('characters', c.entityName);
        const animCfg  = atlasConfigsRef.current.get(atlasKey)?.animations[animName];

        drawSprite(img, animCfg, c.id,
          (c.x / 100) * W, (c.y / 100) * H,
          (c.sceneHeight / 100) * H * c.scale, now);
      }

      rafRef.current = requestAnimationFrame(render);
    };

    rafRef.current = requestAnimationFrame(render);
    return () => { if (rafRef.current !== undefined) cancelAnimationFrame(rafRef.current); };
  }, [canvasRef]);
}
