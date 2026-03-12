/**
 * useCanvasRenderLoop
 *
 * RAF render loop — starts once on mount, never restarts.
 * Reads all mutable state via refs to avoid loop teardown.
 *
 * Renders three layers (bottom → top):
 *   1. Background  (animated strip, stretched to viewport)
 *   2. Furniture   (z-sorted, selected/hovered highlight)
 *   3. Characters  (z-sorted, per-slot animation)
 */

import { useEffect } from 'react';
import type { LocationConfig } from '../types/location.types';
import type { AnimationConfig, SlotState } from './canvasTypes';
import type { CanvasAssetsRefs } from './useCanvasAssets';
import { atlasUrl, atlasConfigUrl } from './useCanvasAssets';
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
  timeOfDayRef: React.MutableRefObject<string>;
}

// ── Frame helpers ────────────────────────────────────────────────────────────────

function frameW(img: HTMLImageElement, cfg: AnimationConfig): number {
  return cfg.frameWidth > 0 ? cfg.frameWidth : Math.floor(img.naturalWidth / cfg.columns);
}

function frameH(img: HTMLImageElement, cfg: AnimationConfig): number {
  return cfg.frameHeight > 0 ? cfg.frameHeight : img.naturalHeight;
}

// ── Hook ───────────────────────────────────────────────────────────────────

export function useCanvasRenderLoop({
  canvasRef,
  viewportRef,
  assetsRefs,
  configRef,
  selectedRef,
  hoveredRef,
  charAnimsRef,
  rafRef,
  timeOfDayRef,
}: CanvasRenderLoopOptions): void {
  const { imagesRef, atlasConfigsRef, slotStateRef } = assetsRefs;

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // ── drawSprite ───────────────────────────────────────────────────────────────
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
        if (flipX) ctx.restore();
        return;
      }

      const fw = frameW(img, animCfg);
      const fh = frameH(img, animCfg);

      let frame = 0;
      if (slotId !== null) {
        let state: SlotState | undefined = slotStateRef.current.get(slotId);

        const isCharacter = !slotId.startsWith('furniture_') && !slotId.startsWith('__');
        const currentAnim = isCharacter ? charAnimsRef.current?.[slotId] : undefined;

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
      ctx.drawImage(img, frame * fw, 0, fw, fh, drawX, drawY, drawW, drawH);
      if (flipX) ctx.restore();
    };

    // ── drawBackground ────────────────────────────────────────────────────────────
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
        state.frameIndex    = next;
        state.lastFrameTime = now;
      }

      if (!animCfg || animCfg.columns <= 1) {
        ctx.drawImage(img, 0, 0, img.naturalWidth, img.naturalHeight, vpX, vpY, vpW, vpH);
        return;
      }

      ctx.drawImage(img, state.frameIndex * fw, 0, fw, fh, vpX, vpY, vpW, vpH);
    };

    // ── render ──────────────────────────────────────────────────────────────────
    const render = (now: number): void => {
      const bufW = canvas.width;
      const bufH = canvas.height;
      ctx.clearRect(0, 0, bufW, bufH);
      ctx.imageSmoothingEnabled = false;

      ctx.fillStyle = '#1a1a2e';
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
      const timeOfDay  = timeOfDayRef.current;
      const bgAnimName = cfg.backgroundAnimations?.[timeOfDay] ?? cfg.backgroundAnimation;
      const bgUrl      = atlasUrl('locations', cfg.locationAsset, bgAnimName);
      const bgImg      = imagesRef.current.get(bgUrl);

      if (bgImg) {
        const bgAtlasKey = atlasConfigUrl('locations', cfg.locationAsset);
        const bgAnimCfg  = atlasConfigsRef.current.get(bgAtlasKey)?.animations[bgAnimName];
        drawBackground(bgImg, bgAnimCfg, vpX, vpY, vpW, vpH, now);
      } else {
        ctx.fillStyle = '#1a1a2e';
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
          img, animCfg, 'furniture_' + f.id,
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
