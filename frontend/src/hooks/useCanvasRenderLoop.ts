/**
 * useCanvasRenderLoop
 *
 * RAF render loop — starts once on mount, never restarts.
 * Reads all mutable state via refs to avoid loop teardown.
 *
 * Renders three layers (bottom → top):
 *   1. Background  (sprite-atlas rows resolved via resolveActiveRow)
 *   2. Furniture   (z-sorted, selected/hovered highlight)
 *   3. Characters  (z-sorted, per-slot animation)
 */

import { useEffect } from 'react';
import type { LocationConfig } from '../types/location.types';
import type { AnimationConfig, GameStateSnapshot, SlotState } from './canvasTypes';
import {
  SCENE_FALLBACK_COLOR,
  BACKGROUND_SLOT_KEY,
  FURNITURE_SLOT_PREFIX,
  getSlotKind,
} from './canvasTypes';
import type { CanvasAssetsRefs } from './useCanvasAssets';
import { atlasUrl, atlasConfigUrl } from './useCanvasAssets';
import { resolveActiveRow, getRowPlayback } from './atlasUtils';
import React from 'react';

export interface CanvasRenderLoopOptions {
  canvasRef: React.RefObject<HTMLCanvasElement>;
  viewportRef: React.RefObject<{ vpX: number; vpY: number; vpW: number; vpH: number }>;
  assetsRefs: CanvasAssetsRefs;
  configRef: React.MutableRefObject<LocationConfig>;
  selectedRef: React.MutableRefObject<string | null>;
  hoveredRef: React.MutableRefObject<string | null>;
  charAnimsRef: React.MutableRefObject<Record<string, string> | undefined>;
  rafRef: React.MutableRefObject<number | undefined>;
  gameStateRef: React.MutableRefObject<GameStateSnapshot>;
}

// ── Frame dimension helpers ───────────────────────────────────────────────────────

function frameW(img: HTMLImageElement, cfg: AnimationConfig): number {
  return cfg.frameWidth > 0 ? cfg.frameWidth : Math.floor(img.naturalWidth / cfg.columns);
}

function frameH(img: HTMLImageElement, cfg: AnimationConfig): number {
  return cfg.frameHeight > 0 ? cfg.frameHeight : img.naturalHeight;
}

// ── Shared helpers ───────────────────────────────────────────────────────────────

/** Advance a SlotState frame counter by one tick if the interval has elapsed. */
function advanceFrame(state: SlotState, columns: number, fps: number, loop: boolean, now: number): void {
  const interval = 1000 / fps;
  if (now - state.lastFrameTime >= interval) {
    let next = state.frameIndex + 1;
    if (next >= columns) next = loop ? 0 : columns - 1;
    state.frameIndex    = next;
    state.lastFrameTime = now;
  }
}

interface DestRect { drawX: number; drawY: number; drawW: number; drawH: number; }

/** Compute destination rect, respecting cropOffset if present. */
function computeDestRect(
  animCfg: AnimationConfig,
  fw: number,
  fh: number,
  destX: number,
  destY: number,
  destH: number,
): DestRect {
  if (animCfg.cropOffset) {
    const crop = animCfg.cropOffset;
    const scaleFactor = destH / crop.originalHeight;
    const fullW = crop.originalWidth * scaleFactor;
    const fullX = destX - fullW / 2;
    const fullY = destY - destH;
    return {
      drawX: fullX + crop.x * scaleFactor,
      drawY: fullY + crop.y * scaleFactor,
      drawW: fw * scaleFactor,
      drawH: fh * scaleFactor,
    };
  }
  const dw = fh > 0 ? destH * (fw / fh) : destH;
  return {
    drawX: destX - dw / 2,
    drawY: destY - destH,
    drawW: dw,
    drawH: destH,
  };
}

// ── Hook ──────────────────────────────────────────────────────────────────────

export function useCanvasRenderLoop({
  canvasRef,
  viewportRef,
  assetsRefs,
  configRef,
  selectedRef,
  hoveredRef,
  charAnimsRef,
  rafRef,
  gameStateRef,
}: CanvasRenderLoopOptions): void {
  const { imagesRef, atlasConfigsRef, slotStateRef } = assetsRefs;

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // ── drawSprite ────────────────────────────────────────────────────────────
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
      const fw = animCfg ? frameW(img, animCfg) : img.naturalWidth;
      const fh = animCfg ? frameH(img, animCfg) : img.naturalHeight;

      let frame = 0;

      if (animCfg && animCfg.columns > 1 && slotId !== null) {
        let state = slotStateRef.current.get(slotId);

        if (getSlotKind(slotId) === 'character') {
          const currentAnim = charAnimsRef.current?.[slotId];
          if (state && currentAnim && state.animationName !== currentAnim) {
            state = { animationName: currentAnim, frameIndex: 0, lastFrameTime: now, activeRowIndex: animCfg.defaultRow ?? 0, kind: 'character' };
            slotStateRef.current.set(slotId, state);
          }
        }

        if (state) {
          const activeRow = resolveActiveRow(animCfg.rows, animCfg.defaultRow, gameStateRef.current);
          state.activeRowIndex = activeRow;
          const { fps, loop } = getRowPlayback(animCfg.rows, activeRow, animCfg.defaultRow);
          advanceFrame(state, animCfg.columns, fps, loop, now);
          frame = state.frameIndex;
        }
      }

      const srcRow = (() => {
        if (!animCfg || animCfg.columns <= 1) return 0;
        return slotStateRef.current.get(slotId ?? '')?.activeRowIndex ?? animCfg.defaultRow;
      })();
      const srcY = srcRow * fh;

      const { drawX, drawY, drawW, drawH } = animCfg
        ? computeDestRect(animCfg, fw, fh, destX, destY, destH)
        : {
            drawX: destX - (img.naturalWidth > 0 ? destH * (img.naturalWidth / img.naturalHeight) : destH) / 2,
            drawY: destY - destH,
            drawW: img.naturalWidth > 0 ? destH * (img.naturalWidth / img.naturalHeight) : destH,
            drawH: destH,
          };

      if (flipX) { ctx.save(); ctx.translate(destX, 0); ctx.scale(-1, 1); ctx.translate(-destX, 0); }
      ctx.drawImage(img, frame * fw, srcY, fw, fh, drawX, drawY, drawW, drawH);
      if (flipX) ctx.restore();
    };

    // ── drawBackground ───────────────────────────────────────────────────────
    const drawBackground = (
      img: HTMLImageElement,
      animCfg: AnimationConfig | undefined,
      vpX: number,
      vpY: number,
      vpW: number,
      vpH: number,
      now: number
    ): void => {
      const fw = animCfg ? frameW(img, animCfg) : img.naturalWidth;
      const fh = animCfg ? frameH(img, animCfg) : img.naturalHeight;

      let state = slotStateRef.current.get(BACKGROUND_SLOT_KEY);
      if (!state) {
        state = { animationName: '__bg__', frameIndex: 0, lastFrameTime: now, activeRowIndex: 0, kind: 'background' };
        slotStateRef.current.set(BACKGROUND_SLOT_KEY, state);
      }

      if (!animCfg || animCfg.columns <= 1) {
        ctx.drawImage(img, 0, 0, img.naturalWidth, img.naturalHeight, vpX, vpY, vpW, vpH);
        return;
      }

      const hasRows = animCfg.rows && animCfg.rows.length > 0;
      let activeRowIndex: number;
      let fps: number;
      let loop: boolean;

      if (hasRows) {
        activeRowIndex = resolveActiveRow(animCfg.rows, animCfg.defaultRow, gameStateRef.current);
        const playback = getRowPlayback(animCfg.rows, activeRowIndex, animCfg.defaultRow);
        fps  = playback.fps;
        loop = playback.loop;
      } else {
        activeRowIndex = 0;
        fps  = animCfg.fps ?? 1;
        loop = animCfg.loop !== false;
      }

      state.activeRowIndex = activeRowIndex;
      advanceFrame(state, animCfg.columns, fps, loop, now);

      const srcY = activeRowIndex * fh;
      ctx.drawImage(img, state.frameIndex * fw, srcY, fw, fh, vpX, vpY, vpW, vpH);
    };

    // ── render ───────────────────────────────────────────────────────────────
    const render = (now: number): void => {
      const bufW = canvas.width;
      const bufH = canvas.height;
      ctx.clearRect(0, 0, bufW, bufH);
      ctx.imageSmoothingEnabled = false;

      ctx.fillStyle = SCENE_FALLBACK_COLOR;
      ctx.fillRect(0, 0, bufW, bufH);

      const { vpX, vpY, vpW, vpH } = viewportRef.current ??
        { vpX: 0, vpY: 0, vpW: bufW, vpH: bufH };

      const cfg      = configRef.current;
      const selected = selectedRef.current;
      const hovered  = hoveredRef.current;

      ctx.save();
      ctx.beginPath();
      ctx.rect(vpX, vpY, vpW, vpH);
      ctx.clip();

      // Layer 1: Background
      const bgAnimName = cfg.backgroundAnimation;
      const bgUrl      = atlasUrl('locations', cfg.locationAsset, bgAnimName);
      const bgImg      = imagesRef.current.get(bgUrl);

      if (bgImg) {
        const bgAtlasKey = atlasConfigUrl('locations', cfg.locationAsset);
        const bgAnimCfg  = atlasConfigsRef.current.get(bgAtlasKey)?.animations[bgAnimName];
        drawBackground(bgImg, bgAnimCfg, vpX, vpY, vpW, vpH, now);
      } else {
        ctx.fillStyle = SCENE_FALLBACK_COLOR;
        ctx.fillRect(vpX, vpY, vpW, vpH);
      }

      // Layer 2: Furniture
      const furniture = [...cfg.furniture].sort((a, b) => a.zOrder - b.zOrder);
      for (const f of furniture) {
        const url    = atlasUrl('furniture', f.entityName, f.animation);
        const img    = imagesRef.current.get(url);
        if (!img) continue;
        const atlasKey = atlasConfigUrl('furniture', f.entityName);
        const animCfg  = atlasConfigsRef.current.get(atlasKey)?.animations[f.animation];

        ctx.save();
        if      (f.id === selected) { ctx.shadowColor = '#4a90e2'; ctx.shadowBlur = 12; }
        else if (f.id === hovered)  { ctx.shadowColor = '#ffffff'; ctx.shadowBlur = 6;  }

        drawSprite(
          img, animCfg, FURNITURE_SLOT_PREFIX + f.id,
          vpX + (f.x / 100) * vpW,
          vpY + (f.y / 100) * vpH,
          (f.sceneHeight / 100) * vpH * f.scale,
          now, f.flipX ?? false
        );
        ctx.restore();
      }

      // Layer 3: Characters
      const characters = [...cfg.characters].sort((a, b) => a.zOrder - b.zOrder);
      for (const c of characters) {
        const animName = charAnimsRef.current?.[c.id] ?? c.defaultAnimation;
        const url      = atlasUrl('characters', c.entityName, animName);
        const img      = imagesRef.current.get(url);
        if (!img) continue;
        const atlasKey = atlasConfigUrl('characters', c.entityName);
        const animCfg  = atlasConfigsRef.current.get(atlasKey)?.animations[animName];

        drawSprite(
          img, animCfg, c.id,
          vpX + (c.x / 100) * vpW,
          vpY + (c.y / 100) * vpH,
          (c.sceneHeight / 100) * vpH * c.scale,
          now
        );
      }

      ctx.restore();
      rafRef.current = requestAnimationFrame(render);
    };

    rafRef.current = requestAnimationFrame(render);
    return () => {
      if (rafRef.current !== undefined) cancelAnimationFrame(rafRef.current);
    };
  }, [canvasRef, viewportRef]);
}
