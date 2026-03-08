/**
 * LocationRenderer — compositing layer approach.
 *
 * Layer 0: Background — static composite PNG from location asset (fills scene)
 * Layer 1: Ambient — CSS time-of-day color overlay
 * Layer 2: Furniture — scaled composite PNGs positioned in scene
 * Layer 3: Characters — SpriteAnimator with atlas animations
 *
 * NO atlas loading for background or furniture.
 * Furniture uses pre-rendered composite PNGs scaled to scene.
 */
import { memo, useCallback, useEffect, useState } from 'react';
import { PixelScene } from '@/components/shared/PixelScene/PixelScene';
import { SpriteAnimator } from '@/components/shared/SpriteAnimator/SpriteAnimator';
import { getCompositeUrl, loadAtlasConfig } from '@/services/assetService';
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

  /* ── Load character atlas configs for relative height ── */
  const [charAtlases, setCharAtlases] = useState<Record<string, AtlasConfig>>({});

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

      {/* ═══ LAYER 0: Background composite ═══ */}
      <div className="pixel-scene__layer" style={{ zIndex: 0 }}>
        <img
          className="location-renderer__bg"
          src={getCompositeUrl('locations', config.locationAsset)}
          alt={config.name}
          draggable={false}
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

      {/* ═══ LAYER 2: Furniture composites ═══ */}
      <div
        className="pixel-scene__layer pixel-scene__layer--interactive"
        style={{ zIndex: 10 }}
      >
        {config.furniture.map((item) => {
          const isClickable = Boolean(item.actionCode);
          const isSelected = selectedObjectId === item.id;
          const compositeUrl = getCompositeUrl('furniture', item.entityName);

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
              <img
                className="location-renderer__furniture"
                src={compositeUrl}
                alt={item.label ?? item.entityName}
                draggable={false}
                style={{
                  height: `${SCENE_HEIGHT * (item.sceneHeight ?? 0.30) * (item.scale ?? 1)}px`,
                  width: 'auto',
                  imageRendering: 'pixelated',
                }}
              />
              {item.label && (
                <span className="pixel-scene__label">{item.label}</span>
              )}
            </div>
          );
        })}
      </div>

      {/* ═══ LAYER 3: Characters (animated sprites) ═══ */}
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
