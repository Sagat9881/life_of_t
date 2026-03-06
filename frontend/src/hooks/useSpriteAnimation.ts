/**
 * Hook for managing sprite animation state.
 * Loads sprite-atlas.json, resolves animation (strip or grid), and cycles frames.
 */
import { useCallback, useEffect, useRef, useState } from 'react';
import type { SpriteAnimation, SpriteAnimationState } from '@/types/sprite';
import { loadAtlasConfig, resolveAnimation, preloadAtlasImage } from '@/services/assetService';

export interface UseSpriteAnimationOptions {
  readonly entityType: string;
  readonly entityName: string;
  readonly animation: string;
  readonly playing?: boolean | undefined;
  readonly onComplete?: (() => void) | undefined;
  /** Condition value for grid row selection (e.g. "morning", "evening") */
  readonly condition?: string | undefined;
}

export const useSpriteAnimation = ({
  entityType,
  entityName,
  animation,
  playing = true,
  onComplete,
  condition,
}: UseSpriteAnimationOptions): SpriteAnimationState => {
  const [state, setState] = useState<SpriteAnimationState>({
    currentFrame: 0,
    isLoaded: false,
    isPlaying: false,
    error: null,
    animation: null,
  });

  const rafRef = useRef<number>(0);
  const lastFrameTimeRef = useRef<number>(0);
  const currentFrameRef = useRef<number>(0);
  const animationRef = useRef<SpriteAnimation | null>(null);

  // Load atlas config and resolve animation
  useEffect(() => {
    let cancelled = false;

    const load = async (): Promise<void> => {
      try {
        setState((prev) => ({ ...prev, isLoaded: false, error: null }));

        const config = await loadAtlasConfig(entityType, entityName);
        if (cancelled) return;

        const resolved = resolveAnimation(entityType, entityName, animation, config, condition);
        if (cancelled) return;

        if (!resolved) {
          setState((prev) => ({
            ...prev,
            error: `Animation '${animation}' not found for ${entityType}/${entityName}`,
            isLoaded: false,
          }));
          return;
        }

        await preloadAtlasImage(resolved.atlasUrl);
        if (cancelled) return;

        animationRef.current = resolved;
        currentFrameRef.current = 0;

        setState({
          currentFrame: 0,
          isLoaded: true,
          isPlaying: playing ?? true,
          error: null,
          animation: resolved,
        });
      } catch (err) {
        if (!cancelled) {
          setState((prev) => ({
            ...prev,
            error: err instanceof Error ? err.message : 'Unknown error loading animation',
            isLoaded: false,
          }));
        }
      }
    };

    void load();

    return () => {
      cancelled = true;
    };
  }, [entityType, entityName, animation, playing, condition]);

  // Animation frame loop
  const tick = useCallback((timestamp: number) => {
    const anim = animationRef.current;
    if (!anim) return;

    const frameDuration = 1000 / anim.fps;

    if (timestamp - lastFrameTimeRef.current >= frameDuration) {
      lastFrameTimeRef.current = timestamp;
      const nextFrame = currentFrameRef.current + 1;

      if (nextFrame >= anim.frameCount) {
        if (anim.loop) {
          currentFrameRef.current = 0;
        } else {
          currentFrameRef.current = anim.frameCount - 1;
          setState((prev) => ({ ...prev, currentFrame: currentFrameRef.current, isPlaying: false }));
          onComplete?.();
          return;
        }
      } else {
        currentFrameRef.current = nextFrame;
      }

      setState((prev) => ({ ...prev, currentFrame: currentFrameRef.current }));
    }

    rafRef.current = requestAnimationFrame(tick);
  }, [onComplete]);

  // Start/stop animation loop
  useEffect(() => {
    const isPlaying = playing ?? true;
    if (state.isLoaded && isPlaying) {
      lastFrameTimeRef.current = 0;
      rafRef.current = requestAnimationFrame(tick);
      setState((prev) => ({ ...prev, isPlaying: true }));
    }

    return () => {
      if (rafRef.current) {
        cancelAnimationFrame(rafRef.current);
        rafRef.current = 0;
      }
    };
  }, [state.isLoaded, playing, tick]);

  return state;
};
