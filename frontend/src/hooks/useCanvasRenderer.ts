/**
 * useCanvasRenderer Hook
 * 
 * Manages canvas rendering lifecycle:
 * - Loads sprite atlases
 * - Renders layers (background, furniture, characters)
 * - Handles animations via requestAnimationFrame
 */

import { useEffect, useRef } from 'react';
import type { LocationConfig } from '../types/location.types';

interface UseCanvasRendererOptions {
  config: LocationConfig;
  canvasRef: React.RefObject<HTMLCanvasElement>;
  timeOfDay: string;
  selectedObjectId: string | null;
  hoveredObjectId: string | null;
  characterAnimations?: Record<string, { animationName: string; frameIndex: number }>;
}

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

    // Preload assets
    const loadAssets = async () => {
      const assetsToLoad: string[] = [];

      // Background layers
      if (config.backgroundLayers) {
        config.backgroundLayers.forEach(layer => {
          const path = layer.variations?.[timeOfDay] || layer.path;
          if (path) assetsToLoad.push(path);
        });
      }

      // Furniture sprites
      if (config.furniture) {
        config.furniture.forEach(f => {
          if (f.spritePath) assetsToLoad.push(f.spritePath);
        });
      }

      // Load all
      await Promise.all(
        assetsToLoad.map(path => {
          return new Promise<void>((resolve) => {
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
              console.error(`Failed to load: ${path}`);
              resolve(); // Don't block render
            };
            img.src = path;
          });
        })
      );
    };

    // Render loop
    const render = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      // 1. Background layers
      if (config.backgroundLayers) {
        config.backgroundLayers.forEach(layer => {
          const path = layer.variations?.[timeOfDay] || layer.path;
          if (!path) return;

          const img = loadedAssetsRef.current.get(path);
          if (img && img.complete) {
            const x = layer.offset?.x || 0;
            const y = layer.offset?.y || 0;
            ctx.drawImage(img, x, y);
          }
        });
      }

      // 2. Furniture (sorted by zOrder)
      if (config.furniture) {
        const sorted = [...config.furniture].sort((a, b) => (a.zOrder || 0) - (b.zOrder || 0));
        sorted.forEach(f => {
          if (!f.spritePath || !f.x || !f.y) return;

          const img = loadedAssetsRef.current.get(f.spritePath);
          if (img && img.complete) {
            ctx.save();

            // Highlight if selected/hovered
            if (f.id === selectedObjectId) {
              ctx.shadowColor = '#4a90e2';
              ctx.shadowBlur = 10;
            } else if (f.id === hoveredObjectId) {
              ctx.shadowColor = '#ffffff';
              ctx.shadowBlur = 5;
            }

            ctx.drawImage(img, f.x, f.y);
            ctx.restore();
          }
        });
      }

      // 3. Characters (if any)
      // TODO: Render character sprites based on characterAnimations

      animationFrameRef.current = requestAnimationFrame(render);
    };

    loadAssets().then(() => {
      render();
    });

    return () => {
      if (animationFrameRef.current) {
        cancelAnimationFrame(animationFrameRef.current);
      }
    };
  }, [config, canvasRef, timeOfDay, selectedObjectId, hoveredObjectId, characterAnimations]);
}
