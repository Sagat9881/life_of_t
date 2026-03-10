/**
 * useSpriteAnimation — loads sprite-atlas.json and drives frame index.
 *
 * Returns the current frame number and resolved animation metadata.
 * Currently implemented as a lightweight stub that loads atlas JSON
 * from the standard URL and advances frames via requestAnimationFrame.
 *
 * Full implementation lives in useCanvasRenderer (Canvas path).
 * This hook is used only by the legacy CSS-background SpriteAnimator,
 * which is kept for compatibility but not used in the main render path.
 */
import { useState, useEffect, useRef } from 'react';

export interface AnimationMeta {
  atlasUrl:    string;
  frameWidth:  number;
  frameHeight: number;
  frameCount:  number;
  fps:         number;
  loop:        boolean;
  currentRow:  number;
  totalRows:   number;
  renderMode?: 'sprite' | 'overlay';
  opacity?:    number;
  cropOffset?: { x: number; y: number };
}

export interface UseSpriteAnimationOptions {
  entityType:  string;
  entityName:  string;
  animation:   string;
  playing?:    boolean;
  condition?:  string;
  onComplete?: () => void;
}

export interface UseSpriteAnimationResult {
  currentFrame: number;
  isLoaded:     boolean;
  error:        string | null;
  animation:    AnimationMeta | null;
}

interface AtlasAnimationConfig {
  file:        string;
  layout:      'strip' | 'grid';
  columns:     number;
  rows?:       number;
  frameWidth:  number;
  frameHeight: number;
  fps:         number;
  loop:        boolean;
  cropOffset?: { x: number; y: number; originalWidth: number; originalHeight: number };
}

interface AtlasConfig {
  entity:     string;
  animations: Record<string, AtlasAnimationConfig>;
}

function atlasConfigUrl(entityType: string, entityName: string): string {
  return `/assets/${entityType}/${entityName}/sprite-atlas.json`;
}

export function useSpriteAnimation({
  entityType,
  entityName,
  animation,
  playing = true,
  onComplete,
}: UseSpriteAnimationOptions): UseSpriteAnimationResult {
  const [meta, setMeta]           = useState<AnimationMeta | null>(null);
  const [isLoaded, setIsLoaded]   = useState(false);
  const [error, setError]         = useState<string | null>(null);
  const [currentFrame, setFrame]  = useState(0);

  const frameRef    = useRef(0);
  const rafRef      = useRef<number | undefined>(undefined);
  const lastTimeRef = useRef(0);

  // Load atlas config
  useEffect(() => {
    let cancelled = false;
    setIsLoaded(false);
    setError(null);
    setMeta(null);
    setFrame(0);
    frameRef.current = 0;

    fetch(atlasConfigUrl(entityType, entityName))
      .then((r) => { if (!r.ok) throw new Error(`${r.status} ${atlasConfigUrl(entityType, entityName)}`); return r.json() as Promise<AtlasConfig>; })
      .then((cfg) => {
        if (cancelled) return;
        const animCfg = cfg.animations[animation];
        if (!animCfg) { setError(`Animation '${animation}' not found`); return; }
        const frameCount = animCfg.columns;
        const totalRows  = animCfg.rows ?? 1;
        setMeta({
          atlasUrl:   `/assets/${entityType}/${entityName}/animations/${animCfg.file}`,
          frameWidth: animCfg.frameWidth,
          frameHeight:animCfg.frameHeight,
          frameCount,
          fps:        animCfg.fps,
          loop:       animCfg.loop,
          currentRow: 0,
          totalRows,
          cropOffset: animCfg.cropOffset ? { x: animCfg.cropOffset.x, y: animCfg.cropOffset.y } : undefined,
        });
        setIsLoaded(true);
      })
      .catch((e: unknown) => { if (!cancelled) setError(e instanceof Error ? e.message : String(e)); });

    return () => { cancelled = true; };
  }, [entityType, entityName, animation]);

  // RAF frame ticker
  useEffect(() => {
    if (!meta || !playing) return;
    const interval = 1000 / meta.fps;

    const tick = (now: number) => {
      if (now - lastTimeRef.current >= interval) {
        lastTimeRef.current = now;
        frameRef.current += 1;
        if (frameRef.current >= meta.frameCount) {
          if (meta.loop) {
            frameRef.current = 0;
          } else {
            frameRef.current = meta.frameCount - 1;
            onComplete?.();
            return; // stop RAF
          }
        }
        setFrame(frameRef.current);
      }
      rafRef.current = requestAnimationFrame(tick);
    };

    rafRef.current = requestAnimationFrame(tick);
    return () => { if (rafRef.current !== undefined) cancelAnimationFrame(rafRef.current); };
  }, [meta, playing, onComplete]);

  return { currentFrame, isLoaded, error, animation: meta };
}
