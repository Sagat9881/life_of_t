/**
 * useCanvasRenderer Hook
 *
 * Manages canvas rendering lifecycle:
 * - Loads sprite atlases from backend /assets/** endpoint
 * - Renders layers: background → furniture (z-sorted) → characters
 * - Drives animation via requestAnimationFrame
 *
 * Asset URL convention (matches LayeredAssetGenerator output):
 *   /assets/{type}/{name}/animations/{animationName}_atlas.png
 *
 * characterAnimations: Record<string, string>
 *   key   = CharacterSlot.id  (e.g. 'tanya')
 *   value = animation name    (e.g. 'idle', 'walk', 'sleep')
 *   frameIndex is managed internally by the RAF loop.
 */

import { useEffect, useRef } from 'react';
import type { LocationConfig, FurniturePlacement, CharacterSlot } from '../types/location.types';

interface UseCanvasRendererOptions {
  config: LocationConfig;
  canvasRef: React.RefObject<HTMLCanvasElement>;
  timeOfDay: string;
  selectedObjectId: string | null;
  hoveredObjectId: string | null;
  /** animationName per character slot id */
  characterAnimations?: Record<string, string>;
}

/** Build the backend URL for a sprite atlas PNG. */
const atlasUrl = (type: string, name: string, animation: string): string =>
  `/assets/${type}/${name}/animations/${animation}_atlas.png`;

export function useCanvasRenderer({
  config,
  canvasRef,
  timeOfDay,
  selectedObjectId,
  hoveredObjectId,
  characterAnimations,
}: UseCanvasRendererOptions) {
  const animationFrameRef = useRef<number>();
  const loadedAssetsRef = useRef<Map<string, HTMLImageElement>>(new Map());

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // ── ASSET LOADING ─────────────────────────────────────────────────
    const loadAssets = async (): Promise<void> => {
      const paths: string[] = [];

      // Background atlas — served by backend under /assets/locations/...
      paths.push(atlasUrl('locations', config.locationAsset, config.backgroundAnimation));

      // Furniture atlases
      config.furniture.forEach((f: FurniturePlacement) => {
        paths.push(atlasUrl('furniture', f.entityName, f.animation));
      });

      // Character atlases — use current animation from characterAnimations or default
      config.characters.forEach((c: CharacterSlot) => {
        const animName = characterAnimations?.[c.id] ?? c.defaultAnimation;
        paths.push(atlasUrl('characters', c.entityName, animName));
      });

      await Promise.all(
        paths.map((path) =>
          new Promise<void>((resolve) => {
            if (loadedAssetsRef.current.has(path)) {
              resolve();
              return;
            }
            const img = new Image();
            img.onload = () => {
              loadedAssetsRef.current.set(path, img);
              resolve();
            };
            img.onerror = () => {
              console.error(`[useCanvasRenderer] Failed to load: ${path}`);
              resolve();
            };
            img.src = path;
          })
        )
      );
    };

    // ── RENDER LOOP ─────────────────────────────────────────────────
    const render = (): void => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      // 1. Background
      const bgPath = atlasUrl('locations', config.locationAsset, config.backgroundAnimation);
      const bgImg = loadedAssetsRef.current.get(bgPath);
      if (bgImg?.complete) {
        ctx.drawImage(bgImg, 0, 0, canvas.width, canvas.height);
      }

      // 2. Furniture — sorted by zOrder, positions in % of canvas
      const sortedFurniture = [...config.furniture].sort(
        (a: FurniturePlacement, b: FurniturePlacement) => a.zOrder - b.zOrder
      );
      sortedFurniture.forEach((f: FurniturePlacement) => {
        const path = atlasUrl('furniture', f.entityName, f.animation);
        const img = loadedAssetsRef.current.get(path);
        if (!img?.complete) return;

        ctx.save();

        if (f.id === selectedObjectId) {
          ctx.shadowColor = '#4a90e2';
          ctx.shadowBlur = 10;
        } else if (f.id === hoveredObjectId) {
          ctx.shadowColor = '#ffffff';
          ctx.shadowBlur = 5;
        }

        const x = (f.x / 100) * canvas.width;
        const y = (f.y / 100) * canvas.height;
        const height = (f.sceneHeight / 100) * canvas.height * f.scale;
        const width =
          img.naturalWidth > 0 && img.naturalHeight > 0
            ? height * (img.naturalWidth / img.naturalHeight)
            : height;

        ctx.drawImage(img, x - width / 2, y - height, width, height);
        ctx.restore();
      });

      // 3. Characters — sorted by zOrder
      const sortedChars = [...config.characters].sort(
        (a: CharacterSlot, b: CharacterSlot) => a.zOrder - b.zOrder
      );
      sortedChars.forEach((c: CharacterSlot) => {
        const animName = characterAnimations?.[c.id] ?? c.defaultAnimation;
        const path = atlasUrl('characters', c.entityName, animName);
        const img = loadedAssetsRef.current.get(path);
        if (!img?.complete) return;

        const x = (c.x / 100) * canvas.width;
        const y = (c.y / 100) * canvas.height;
        const height = (c.sceneHeight / 100) * canvas.height * c.scale;
        const width =
          img.naturalWidth > 0 && img.naturalHeight > 0
            ? height * (img.naturalWidth / img.naturalHeight)
            : height;

        ctx.drawImage(img, x - width / 2, y - height, width, height);
      });

      animationFrameRef.current = requestAnimationFrame(render);
    };

    loadAssets().then(() => {
      render();
    }).catch((err) => {
      console.error('[useCanvasRenderer] Asset load failed:', err);
    });

    return () => {
      if (animationFrameRef.current !== undefined) {
        cancelAnimationFrame(animationFrameRef.current);
      }
    };
  }, [config, canvasRef, timeOfDay, selectedObjectId, hoveredObjectId, characterAnimations]);
}
