/**
 * useCanvasAssets — asset loading for the Canvas pipeline.
 *
 * Effect 1: loads all assets for the current location config (including
 *           prefetch of all backgroundAnimations variants).
 * Effect 2: delta-loads character animation atlases when characterAnimations changes.
 * Effect 3: delta-loads the background atlas for the current timeOfDay if not cached.
 */

import { useEffect } from 'react';
import type { LocationConfig } from '../types/location.types';
import type { AtlasConfig, CanvasAssetsRefs } from './canvasTypes';

export interface UseCanvasAssetsOptions {
  config: LocationConfig;
  characterAnimations: Record<string, string>;
  timeOfDay?: string;
  assetsRefs: CanvasAssetsRefs;
}

// ─── helpers ────────────────────────────────────────────────────────────────

export function atlasUrl(
  category: string,
  assetKey: string,
  animationName: string,
): string {
  return `/assets/${category}/${assetKey}/animations/${animationName}_atlas.png`;
}

export function atlasConfigUrl(category: string, assetKey: string): string {
  return `/assets/${category}/${assetKey}/sprite-atlas.json`;
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

// ─── hook ───────────────────────────────────────────────────────────────────

export function useCanvasAssets({
  config,
  characterAnimations,
  timeOfDay,
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

      // Background: fallback
      enqueueImage('locations', config.locationAsset, config.backgroundAnimation);
      // Background: prefetch all timeOfDay variants
      if (config.backgroundAnimations) {
        Object.values(config.backgroundAnimations).forEach((animName) => {
          enqueueImage('locations', config.locationAsset, animName);
        });
      }
      // Sprite-atlas config (one per location)
      enqueueConfig('locations', config.locationAsset);

      // Furniture
      for (const item of config.furniture) {
        const anim = item.animationKey ?? 'idle';
        enqueueImage('locations', item.assetKey, anim);
        enqueueConfig('locations', item.assetKey);
        // init slot state
        if (!slotStateRef.current.has(item.id)) {
          slotStateRef.current.set(item.id, { currentFrame: 0, lastFrameTime: 0 });
        }
      }

      // Characters
      for (const slot of config.characters) {
        enqueueImage('characters', slot.id, slot.defaultAnimation);
        enqueueConfig('characters', slot.id);
        if (!slotStateRef.current.has(slot.id)) {
          slotStateRef.current.set(slot.id, { currentFrame: 0, lastFrameTime: 0 });
        }
      }

      await Promise.all(toLoad);
    }

    void loadAll();
    return () => { cancelled = true; };
  }, [config]);

  // Effect 2: delta-load character animations
  useEffect(() => {
    if (!characterAnimations) return;
    let cancelled = false;

    const tasks: Array<Promise<void>> = [];

    for (const slot of config.characters) {
      const animName = characterAnimations[slot.id] ?? slot.defaultAnimation;
      const url = atlasUrl('characters', slot.id, animName);
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

  // Effect 3: delta-load background for current timeOfDay
  useEffect(() => {
    if (!timeOfDay || !config.backgroundAnimations) return;

    const animName = config.backgroundAnimations[timeOfDay] ?? config.backgroundAnimation;
    const url = atlasUrl('locations', config.locationAsset, animName);

    if (imagesRef.current.has(url)) return;

    let cancelled = false;
    loadImage(url).then((img) => {
      if (img && !cancelled) imagesRef.current.set(url, img);
    }).catch((e) => console.error('[canvas] timeOfDay delta load error:', e));

    return () => { cancelled = true; };
  }, [timeOfDay, config.locationAsset, config.backgroundAnimation, config.backgroundAnimations]);
}
