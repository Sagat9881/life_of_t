/**
 * LocationRenderer — all entities rendered via SpriteAnimator.
 *
 * Layer 0: Background — SpriteAnimator with location atlas (fills scene)
 * Layer 1: Ambient — CSS time-of-day color overlay
 * Layer 2: Furniture — SpriteAnimator with furniture atlas, positioned in scene
 * Layer 3: Characters + Pets — SpriteAnimator with character atlas
 *
 * Everything uses sprite-atlas.json → SpriteAnimator → animated.
 *
 * ── CROP-AWARE SIZING ──
 * Since v1.4, atlas frames may be cropped to their non-transparent bounding box.
 * frameWidth/frameHeight in sprite-atlas.json now reflect the CROPPED dimensions.
 * LocationRenderer uses these directly — no sceneHeight fudge factors needed.
 * The SpriteAnimator applies cropOffset as CSS translate to position correctly.
 */
import { memo, useCallback, useEffect, useState } from 'react';
import { PixelScene } from '@/components/shared/PixelScene/PixelScene';
import { SpriteAnimator } from '@/components/shared/SpriteAnimator/SpriteAnimator';
import { loadAtlasConfig } from '@/services/assetService';
import { SCENE_HEIGHT } from '@/utils/sceneConstants';
import type { LocationConfig, FurniturePlacement } from '@/config/locations';
import type { AtlasConfig } from '@/types/sprite';
import './LocationRenderer.css';

/* ── Time-of-day ambient overlays ── */
const AMBIENT: Record<string, { color: string; opacity: number; blend: string }> = {
  morning: { color: '#FFE8C0', opacity: 0.08, blend: 'multiply' },
  day:     { color: '#FFFFF0', opacity: 0.0,  blend: 'normal' },
  evening: { color: '#FF8040', opacity: 0.18, blend: 'multiply' },
  night:   { color: '#101830', opacity: 0.50, blend: 'multiply' },
};

const TIME_MAP: Record<string, string> = {
  MORNING: 'morning', DAY: 'day', EVENING: 'evening', NIGHT: 'night',
  morning: 'morning', day: 'day', evening: 'evening', night: 'night',
};

const atlasCache = new Map<string, AtlasConfig>();

export interface LocationRendererProps {
  readonly config: LocationConfig;
  readonly selectedObjectId?: string | null;
  readonly onObjectClick?: (objectId: string, actionCode: string) => void;
  readonly characterAnimations?: Record<string, string>;
  readonly timeOfDay?: string;
}

export const LocationRenderer = memo(function LocationRenderer({
  config,
  selectedObjectId,
  onObjectClick,
  characterAnimations,
  timeOfDay = 'day',
}: LocationRendererProps) {
  const condition = TIME_MAP[timeOfDay] ?? 'day';
  const ambient = AMBIENT[condition] ?? AMBIENT['day']!;

  const handleFurnitureClick = useCallback(
    (f: FurniturePlacement) => {
      if (f.actionCode && onObjectClick) onObjectClick(f.id, f.actionCode);
    },
    [onObjectClick]
  );

  /* ── Load atlas configs for furniture + characters (for relative height) ── */
  const [furnitureAtlases, setFurnitureAtlases] = useState<Record<string, AtlasConfig>>({});
  const [charAtlases, setCharAtlases] = useState<Record<string, AtlasConfig>>({});

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      const result: Record<string, AtlasConfig> = {};
      await Promise.allSettled(
        config.furniture.map(async (f) => {
          const key = `furniture/${f.entityName}`;
          if (atlasCache.has(key)) { result[f.entityName] = atlasCache.get(key)!; return; }
          try {
            const ac = await loadAtlasConfig('furniture', f.entityName);
            atlasCache.set(key, ac);
            result[f.entityName] = ac;
          } catch { /* no atlas — won't render */ }
        })
      );
      if (!cancelled) setFurnitureAtlases(result);
    };
    void load();
    return () => { cancelled = true; };
  }, [config.furniture]);

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      const result: Record<string, AtlasConfig> = {};
      await Promise.allSettled(
        config.characters.map(async (c) => {
          const key = `characters/${c.entityName}`;
          if (atlasCache.has(key)) { result[c.entityName] = atlasCache.get(key)!; return; }
          try {
            const ac = await loadAtlasConfig('characters', c.entityName);
            atlasCache.set(key, ac);
            result[c.entityName] = ac;
          } catch { /* no atlas — render at native size */ }
        })
      );
      if (!cancelled) setCharAtlases(result);
    };
    void load();
    return () => { cancelled = true; };
  }, [config.characters]);

  return (
    <PixelScene className="location-renderer">

      {/* ═══ LAYER 0: Background — animated via SpriteAnimator ═══ */}
      <div className="pixel-scene__layer location-renderer__bg-layer" style={{ zIndex: 0 }}>
        <SpriteAnimator
          entityType="locations"
          entityName={config.locationAsset}
          animation={config.backgroundAnimation}
          sceneRelativeHeight={1.0}
          condition={condition}
        />
      </div>

      {/* ═══ LAYER 1: Ambient time-of-day overlay ═══ */}
      {ambient.opacity > 0 && (
        <div
          className="pixel-scene__layer"
          style={{
            zIndex: 5,
            backgroundColor: ambient.color,
            opacity: ambient.opacity,
            mixBlendMode: ambient.blend as React.CSSProperties['mixBlendMode'],
            pointerEvents: 'none',
          }}
        />
      )}

      {/* ═══ LAYER 2: Furniture — animated via SpriteAnimator ═══ */}
      <div
        className="pixel-scene__layer pixel-scene__layer--interactive"
        style={{ zIndex: 10 }}
      >
        {config.furniture.map((item) => {
          const isClickable = Boolean(item.actionCode);
          const isSelected = selectedObjectId === item.id;
          const fa = furnitureAtlases[item.entityName];
          const entry = fa?.animations[item.animation];

          // With cropped frames, frameHeight already reflects visible content.
          // sceneHeight from config is the fraction of SCENE_HEIGHT to occupy.
          // We use it directly — no need for complex relH calculation.
          const relH = item.sceneHeight;

          return (
            <div
              key={item.id}
              className={[
                'pixel-scene__entity',
                isClickable ? 'pixel-scene__entity--clickable' : '',
                isSelected ? 'pixel-scene__entity--selected' : '',
              ].join(' ')}
              style={{
                left: `${item.x}%`,
                top: `${item.y}%`,
                zIndex: item.zOrder,
              }}
              onClick={isClickable ? () => handleFurnitureClick(item) : undefined}
            >
              <SpriteAnimator
                entityType="furniture"
                entityName={item.entityName}
                animation={item.animation}
                scale={item.scale}
                sceneRelativeHeight={relH}
                condition={condition}
              />
              {item.label && (
                <span className="pixel-scene__label">{item.label}</span>
              )}
            </div>
          );
        })}
      </div>

      {/* ═══ LAYER 3: Characters + Pets (animated sprites) ═══ */}
      <div className="pixel-scene__layer" style={{ zIndex: 50 }}>
        {config.characters.map((char) => {
          const anim = characterAnimations?.[char.entityName] ?? char.defaultAnimation;
          const ca = charAtlases[char.entityName];
          const entry = ca?.animations[anim];
          const relH = entry ? (entry.frameHeight / SCENE_HEIGHT) : undefined;

          return (
            <div
              key={char.id}
              className="pixel-scene__entity"
              style={{
                left: `${char.x}%`,
                top: `${char.y}%`,
                zIndex: char.zOrder,
              }}
            >
              <SpriteAnimator
                entityType="characters"
                entityName={char.entityName}
                animation={anim}
                scale={char.scale}
                sceneRelativeHeight={relH}
                condition={condition}
              />
            </div>
          );
        })}
      </div>
    </PixelScene>
  );
});

export default LocationRenderer;
