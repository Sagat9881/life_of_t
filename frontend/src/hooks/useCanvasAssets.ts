/**
 * useCanvasAssets
 *
 * Manages loading of all atlas PNGs and sprite-atlas.json configs.
 *
 * Effect 1 (dep: config)              — full reload on location change
 * Effect 2 (dep: characterAnimations) — delta-load on animation switch
 *
 * URL scheme:
 *   /assets/{type}/{name}/animations/{animation}_atlas.png
 *   /assets/{type}/{name}/sprite-atlas.json
 */

import { useEffect } from 'react';
import type { LocationConfig, FurniturePlacement, CharacterSlot } from '../types/location.types';
import type { AtlasConfig, SlotState } from './canvasTypes';
import React from 'react';

export interface CanvasAssetsRefs {
  imagesRef: React.MutableRefObject<Map<string, HTMLImageElement>>;
  atlasConfigsRef: React.MutableRefObject<Map<string, AtlasConfig>>;
  slotStateRef: React.MutableRefObject<Map<string, SlotState>>;
}

interface UseCanvasAssetsOptions {
  config: LocationConfig;
  characterAnimations?: Record<string, string>;
  assetsRefs: CanvasAssetsRefs;
}

// ── URL utilities ────────────────────────────────────────────────────────────

export const atlasUrl = (type: string, name: string, animation: string): string =>
  `/assets/${type}/${name}/animations/${animation}_atlas.png`;

export const atlasConfigUrl = (type: string, name: string): string =>
  `/assets/${type}/${name}/sprite-atlas.json`;

// ── Helpers ──────────────────────────────────────────────────────────────────

export async function fetchAtlasConfig(type: string, name: string): Promise<AtlasConfig | null> {
  try {
    const res = await fetch(atlasConfigUrl(type, name));
    if (!res.ok) return null;
    return (await res.json()) as AtlasConfig;
  } catch {
    return null;
  }
}

export function loadImage(src: string): Promise<HTMLImageElement | null> {
  return new Promise((resolve) => {
    const img = new Image();
    img.onload  = () => resolve(img);
    img.onerror = () => { console.warn(`[canvas] 404: ${src}`); resolve(null); };
    img.src = src;
  });
}

// ── Hook ─────────────────────────────────────────────────────────────────────

export function useCanvasAssets({
  config,
  characterAnimations,
  assetsRefs,
}: UseCanvasAssetsOptions): void {
  const { imagesRef, atlasConfigsRef, slotStateRef } = assetsRefs;

  // Effect 1: full asset reload when location config changes
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

      slotStateRef.current.set('__background__', {
        animationName: '__bg__',
        frameIndex: 0,
        lastFrameTime: 0,
      });

      config.furniture.forEach((f: FurniturePlacement) => {
        enqueueImage('furniture', f.entityName, f.animation);
        enqueueConfig('furniture', f.entityName);
        slotStateRef.current.set('furniture_' + f.id, {
          animationName: f.animation,
          frameIndex: 0,
          lastFrameTime: 0,
        });
      });

      config.characters.forEach((c: CharacterSlot) => {
        const anim = characterAnimations?.[c.id] ?? c.defaultAnimation;
        enqueueImage('characters', c.entityName, anim);
        enqueueConfig('characters', c.entityName);
        slotStateRef.current.set(c.id, {
          animationName: anim,
          frameIndex: 0,
          lastFrameTime: 0,
        });
      });

      await Promise.all(work);
    };

    loadAll().catch((e) => console.error('[canvas] load error:', e));
    return () => { cancelled = true; };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [config]);

  // Effect 2: delta-load when a character switches animation
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
}
