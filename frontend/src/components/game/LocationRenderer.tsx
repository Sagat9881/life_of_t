/**
 * LocationRenderer — renders a complete game location with pixel-art sprites.
 *
 * ── RELATIVE SCALING ──
 * The background (location) fills the entire PixelScene viewport (640×480).
 * All other entities (furniture, characters) are sized as a FRACTION of
 * the viewport height, computed from their atlas frameHeight.
 *
 * This guarantees correct proportions regardless of canvas sizes:
 * - Character 128×192: sceneRelativeHeight = 192/480 = 0.40 (40% of scene)
 * - Cat 128×96: sceneRelativeHeight = 96/480 = 0.20 (20% of scene)
 * - Bed 256×192: sceneRelativeHeight = 192/480 = 0.40
 * - Phone 64×80: sceneRelativeHeight = 80/480 = 0.167
 *
 * The `scale` field in LocationConfig is a multiplier on TOP of the
 * relative height. scale=1.0 means "render at native proportions".
 * scale=0.8 means "80% of the natural size".
 */
import { memo, useCallback, useEffect, useState } from 'react';
import { PixelScene } from '@/components/shared/PixelScene/PixelScene';
import { SpriteAnimator } from '@/components/shared/SpriteAnimator/SpriteAnimator';
import {
  getCompositeUrl,
  loadAtlasConfig,
  listOverlayAnimations,
} from '@/services/assetService';
import { SCENE_HEIGHT } from '@/utils/sceneConstants';
import type { LocationConfig, FurniturePlacement } from '@/config/locations';
import type { AtlasConfig } from '@/types/sprite';
import './LocationRenderer.css';

const AMBIENT_FALLBACK: Record<string, { color: string; opacity: number }> = {
  morning: { color: '#E8F4FF', opacity: 0.10 },
  day:     { color: '#FFF8E8', opacity: 0.0 },
  evening: { color: '#FFB060', opacity: 0.15 },
  night:   { color: '#1A1830', opacity: 0.45 },
};

const TIME_SLOT_TO_CONDITION: Record<string, string> = {
  MORNING: 'morning', DAY: 'day', EVENING: 'evening', NIGHT: 'night',
  morning: 'morning', day: 'day', evening: 'evening', night: 'night',
};

/** Cache of loaded atlas configs for computing relative heights */
const atlasCache = new Map<string, AtlasConfig>();

/**
 * Compute sceneRelativeHeight for an entity from its atlas config.
 * Returns frameHeight / SCENE_HEIGHT — so the sprite occupies
 * exactly its native pixel height within the 640×480 scene.
 */
function getRelativeHeight(
  atlasConfig: AtlasConfig | undefined,
  animationName: string
): number | undefined {
  if (!atlasConfig) return undefined;
  const entry = atlasConfig.animations[animationName];
  if (!entry) {
    const first = Object.values(atlasConfig.animations)[0];
    if (!first) return undefined;
    return first.frameHeight / SCENE_HEIGHT;
  }
  return entry.frameHeight / SCENE_HEIGHT;
}

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

  const handleFurnitureClick = useCallback(
    (furniture: FurniturePlacement) => {
      if (furniture.actionCode && onObjectClick) {
        onObjectClick(furniture.id, furniture.actionCode);
      }
    },
    [onObjectClick]
  );

  const condition = TIME_SLOT_TO_CONDITION[timeOfDay] ?? 'day';

  // ── Load location overlay atlas ──
  const [overlayNames, setOverlayNames] = useState<string[]>([]);
  const [atlasLoaded, setAtlasLoaded] = useState(false);

  useEffect(() => {
    let cancelled = false;
    loadAtlasConfig('locations', config.locationAsset)
      .then((ac) => {
        if (cancelled) return;
        atlasCache.set(`locations/${config.locationAsset}`, ac);
        setOverlayNames(listOverlayAnimations(ac));
        setAtlasLoaded(true);
      })
      .catch(() => {
        if (!cancelled) {
          setOverlayNames([]);
          setAtlasLoaded(true);
        }
      });
    return () => { cancelled = true; };
  }, [config.locationAsset]);

  // ── Load furniture atlas configs for relative sizing ──
  const [furnitureAtlases, setFurnitureAtlases] = useState<Record<string, AtlasConfig>>({});

  useEffect(() => {
    let cancelled = false;
    const loadAll = async () => {
      const result: Record<string, AtlasConfig> = {};
      await Promise.allSettled(
        config.furniture.map(async (item) => {
          const key = `furniture/${item.entityName}`;
          if (atlasCache.has(key)) {
            result[item.entityName] = atlasCache.get(key)!;
            return;
          }
          try {
            const ac = await loadAtlasConfig('furniture', item.entityName);
            atlasCache.set(key, ac);
            result[item.entityName] = ac;
          } catch { /* furniture without atlas renders at native size */ }
        })
      );
      if (!cancelled) setFurnitureAtlases(result);
    };
    void loadAll();
    return () => { cancelled = true; };
  }, [config.furniture]);

  // ── Load character atlas configs for relative sizing ──
  const [characterAtlases, setCharacterAtlases] = useState<Record<string, AtlasConfig>>({});

  useEffect(() => {
    let cancelled = false;
    const loadAll = async () => {
      const result: Record<string, AtlasConfig> = {};
      await Promise.allSettled(
        config.characters.map(async (char) => {
          const key = `characters/${char.entityName}`;
          if (atlasCache.has(key)) {
            result[char.entityName] = atlasCache.get(key)!;
            return;
          }
          try {
            const ac = await loadAtlasConfig('characters', char.entityName);
            atlasCache.set(key, ac);
            result[char.entityName] = ac;
          } catch { /* character without atlas renders at fallback size */ }
        })
      );
      if (!cancelled) setCharacterAtlases(result);
    };
    void loadAll();
    return () => { cancelled = true; };
  }, [config.characters]);

  const useFallbackAmbient = atlasLoaded && overlayNames.length === 0;
  const fallbackAmbient = AMBIENT_FALLBACK[condition] ?? AMBIENT_FALLBACK['day']!;

  return (
    <PixelScene className="location-renderer">
      {/* Background — fills entire viewport (640×480) */}
      <div className="pixel-scene__layer" style={{ zIndex: 0 }}>
        <img
          className="location-renderer__bg"
          src={getCompositeUrl('locations', config.locationAsset)}
          alt={config.name}
          draggable={false}
        />
      </div>

      {/* Overlay animations */}
      {overlayNames.map((overlayName) => (
        <div
          key={overlayName}
          className="pixel-scene__layer location-renderer__overlay-anim"
          style={{ zIndex: 5 }}
        >
          <SpriteAnimator
            entityType="locations"
            entityName={config.locationAsset}
            animation={overlayName}
            condition={condition}
            className="location-renderer__overlay-sprite"
          />
        </div>
      ))}

      {/* Fallback ambient */}
      {useFallbackAmbient && fallbackAmbient.opacity > 0 && (
        <div
          className="pixel-scene__layer location-renderer__ambient"
          style={{
            zIndex: 5,
            backgroundColor: fallbackAmbient.color,
            opacity: fallbackAmbient.opacity,
            pointerEvents: 'none',
            mixBlendMode: 'multiply',
          }}
        />
      )}

      {/* Furniture — sized relative to scene viewport */}
      <div
        className="pixel-scene__layer pixel-scene__layer--interactive"
        style={{ zIndex: 10 }}
      >
        {config.furniture.map((item) => {
          const isClickable = Boolean(item.actionCode);
          const isSelected = selectedObjectId === item.id;
          const furnitureAtlas = furnitureAtlases[item.entityName];
          const relHeight = getRelativeHeight(furnitureAtlas, item.animation);

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
                sceneRelativeHeight={relHeight}
                condition={condition}
              />
              {item.label ? (
                <span className="pixel-scene__label">{item.label}</span>
              ) : null}
            </div>
          );
        })}
      </div>

      {/* Characters — sized relative to scene viewport */}
      <div className="pixel-scene__layer" style={{ zIndex: 50 }}>
        {config.characters.map((char) => {
          const anim =
            characterAnimations?.[char.entityName] ?? char.defaultAnimation;
          const charAtlas = characterAtlases[char.entityName];
          const relHeight = getRelativeHeight(charAtlas, anim);

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
                sceneRelativeHeight={relHeight}
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
