/**
 * useCanvasAssets — asset loading for the Canvas pipeline.
 *
 * Effect 1: loads background atlas image + sprite-atlas.json + furniture + character assets
 *           for the current location config.
 * Effect 2: delta-loads atlas images only for slots with explicit overrides in characterAnimations.
 */

import { useEffect } from 'react';
import type { LocationConfig } from '../types/location.types';
import type { AtlasConfig, CanvasAssetsRefs } from './canvasTypes';

export type { CanvasAssetsRefs };

export interface UseCanvasAssetsOptions {
  config: LocationConfig;
  characterAnimations: Record<string, string>;
  assetsRefs: CanvasAssetsRefs;
}

// ─── helpers ─────────────────────────────────────────────────────────────────

export function atlasUrl(
  category: string,
  assetKey: string,
  animationName: string,
): string {
  return `/assets/${category}/${assetKey}/animations/${animationName}_atlas.png`;
}

export function atlasConfigUrl(category: string, assetKey: string): string {
  return `/assets/${category}/${assetKey}/animations/sprite-atlas.json`;
}

async function fetchAtlasConfig(url: string): Promise<AtlasConfig | null> {
  try {
    const res = await fetch(url);
    if (!res.ok) return null;
    return (await res.json()) as AtlasConfig;
  } catch {
    return null;
  }
}

async function loadImage(url: string): Promise<HTMLImageElement | null> {
  return new Promise((resolve) => {
    const img = new Image();
    img.onload  = () => resolve(img);
    img.onerror = () => resolve(null);
    img.src = url;
  });
}

// ─── hook ─────────────────────────────────────────────────────────────────────

export function useCanvasAssets({
  config,
  characterAnimations,
  assetsRefs,
}: UseCanvasAssetsOptions): void {
  const { imagesRef, atlasConfigsRef, slotStateRef } = assetsRefs;

  // Effect 1: full location load
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    let cancelled = false;

    async function loadAll(): Promise<void> {
      const toLoad: Array<Promise<void>> = [];

      function enqueueImage(category: string, assetKey: string, animName: string): void {
        const url = atlasUrl(category, assetKey, animName);
        if (imagesRef.current.has(url)) return;
        toLoad.push(
          loadImage(url).then((img) => {
            if (img && !cancelled) imagesRef.current.set(url, img);
          }),
        );
      }

      function enqueueConfig(category: string, assetKey: string): void {
        const url = atlasConfigUrl(category, assetKey);
        if (atlasConfigsRef.current.has(url)) return;
        toLoad.push(
          fetchAtlasConfig(url).then((cfg) => {
            if (cfg && !cancelled) atlasConfigsRef.current.set(url, cfg);
          }),
        );
      }

      // Background: single base animation + one sprite-atlas.json per location.
      enqueueImage('locations', config.locationAsset, config.backgroundAnimation);
      enqueueConfig('locations', config.locationAsset);

      // Furniture
      for (const item of config.furniture) {
        const anim = item.animation ?? 'idle';
        enqueueImage('furniture', item.entityName, anim);
        enqueueConfig('furniture', item.entityName);
        if (!slotStateRef.current.has(item.id)) {
          slotStateRef.current.set(item.id, { animationName: anim, frameIndex: 0, lastFrameTime: 0, activeRowIndex: 0 });
        }
      }

      // Characters
      for (const slot of config.characters) {
        enqueueImage('characters', slot.entityName, slot.defaultAnimation);
        enqueueConfig('characters', slot.entityName);
        if (!slotStateRef.current.has(slot.id)) {
          slotStateRef.current.set(slot.id, { animationName: slot.defaultAnimation, frameIndex: 0, lastFrameTime: 0, activeRowIndex: 0 });
        }
      }

      await Promise.all(toLoad);
    }

    void loadAll();
    return () => { cancelled = true; };
  }, [config]);

  // Effect 2: delta-load override animations (e.g. tanya post-action anim)
  useEffect(() => {
    if (!characterAnimations) return;
    let cancelled = false;

    const tasks: Array<Promise<void>> = [];

    for (const [slotId, animName] of Object.entries(characterAnimations)) {
      const slot = config.characters.find((s) => s.id === slotId);
      if (!slot) continue;
      const url = atlasUrl('characters', slot.entityName, animName);
      if (!imagesRef.current.has(url)) {
        tasks.push(
          loadImage(url).then((img) => {
            if (img && !cancelled) imagesRef.current.set(url, img);
          }),
        );
      }
    }

    void Promise.all(tasks);
    return () => { cancelled = true; };
  }, [characterAnimations, config.characters]);
}
