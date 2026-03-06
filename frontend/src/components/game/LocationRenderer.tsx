/**
 * LocationRenderer — renders a complete game location with pixel-art sprites.
 *
 * Composes:
 * - PixelScene (container with auto-scaling)
 * - Static background image
 * - Overlay animations from location's sprite-atlas.json (time-of-day ambient)
 * - Hardcoded ambient fallback (if atlas not loaded)
 * - SpriteAnimator for each furniture item
 * - SpriteAnimator for each character
 */
import { memo, useCallback, useEffect, useState } from 'react';
import { PixelScene } from '@/components/shared/PixelScene/PixelScene';
import { SpriteAnimator } from '@/components/shared/SpriteAnimator/SpriteAnimator';
import { getCompositeUrl, loadAtlasConfig, listOverlayAnimations } from '@/services/assetService';
import type { LocationConfig, FurniturePlacement } from '@/config/locations';
import './LocationRenderer.css';

/** Fallback ambient tint colors (used when location has no sprite-atlas.json) */
const AMBIENT_FALLBACK: Record<string, { color: string; opacity: number }> = {
  morning: { color: '#E8F4FF', opacity: 0.10 },
  day:     { color: '#FFF8E8', opacity: 0.0 },
  evening: { color: '#FFB060', opacity: 0.15 },
  night:   { color: '#1A1830', opacity: 0.45 },
};

/** Map game time slot names to condition values used by sprite atlas */
const TIME_SLOT_TO_CONDITION: Record<string, string> = {
  MORNING: 'morning',
  DAY: 'day',
  EVENING: 'evening',
  NIGHT: 'night',
  morning: 'morning',
  day: 'day',
  evening: 'evening',
  night: 'night',
};

export interface LocationRendererProps {
  readonly config: LocationConfig;
  readonly selectedObjectId?: string | null;
  readonly onObjectClick?: (objectId: string, actionCode: string) => void;
  readonly characterAnimations?: Record<string, string>;
  /** Current time-of-day for ambient + grid animation row selection */
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

  // Load location's sprite-atlas.json for overlay animations
  const [overlayNames, setOverlayNames] = useState<string[]>([]);
  const [atlasLoaded, setAtlasLoaded] = useState(false);

  useEffect(() => {
    let cancelled = false;
    loadAtlasConfig('locations', config.locationAsset)
      .then((atlasConfig) => {
        if (cancelled) return;
        const overlays = listOverlayAnimations(atlasConfig);
        setOverlayNames(overlays);
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

  // Use hardcoded fallback only if atlas has no overlay animations
  const useFallbackAmbient = atlasLoaded && overlayNames.length === 0;
  const fallbackAmbient = AMBIENT_FALLBACK[condition] ?? AMBIENT_FALLBACK['day']!;

  return (
    <PixelScene className="location-renderer">
      {/* Background layer — static location image */}
      <div
        className="pixel-scene__layer"
        style={{ zIndex: 0 }}
      >
        <img
          className="location-renderer__bg"
          src={getCompositeUrl('locations', config.locationAsset)}
          alt={config.name}
          draggable={false}
        />
      </div>

      {/* Overlay animations from sprite-atlas.json (replaces hardcoded ambient) */}
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

      {/* Fallback ambient overlay — only if no atlas overlays */}
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

      {/* Furniture layer */}
      <div
        className="pixel-scene__layer pixel-scene__layer--interactive"
        style={{ zIndex: 10 }}
      >
        {config.furniture.map((item) => {
          const isClickable = Boolean(item.actionCode);
          const isSelected = selectedObjectId === item.id;

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
              onClick={
                isClickable ? () => handleFurnitureClick(item) : undefined
              }
            >
              <SpriteAnimator
                entityType="furniture"
                entityName={item.entityName}
                animation={item.animation}
                scale={item.scale}
                condition={condition}
              />
              {item.label ? (
                <span className="pixel-scene__label">{item.label}</span>
              ) : null}
            </div>
          );
        })}
      </div>

      {/* Characters layer */}
      <div
        className="pixel-scene__layer"
        style={{ zIndex: 50 }}
      >
        {config.characters.map((char) => {
          const animation =
            characterAnimations?.[char.entityName] ?? char.defaultAnimation;

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
                animation={animation}
                scale={char.scale}
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
