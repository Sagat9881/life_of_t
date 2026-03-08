/**
 * SpriteAnimator — renders an animated pixel-art sprite from atlas.
 *
 * Supports:
 * - strip layout: single-row horizontal atlas (background-position X)
 * - grid layout: multi-row atlas (background-position X + Y) with condition-based row selection
 * - overlay renderMode: renders atlas image as an overlay with mix-blend-mode
 *   (NOT a plain colored div — uses the actual generated atlas PNG)
 *
 * Scale logic:
 *   CSS size = frameWidth × displayScale × scale
 *   - displayScale comes from sprite-atlas.json (characters=3, locations=1)
 *   - scale is an optional multiplier from the parent (default 1.0)
 */
import { type CSSProperties, memo, useMemo } from 'react';
import type { SpriteAnimatorProps } from '@/types/sprite';
import { useSpriteAnimation } from '@/hooks/useSpriteAnimation';
import type { UseSpriteAnimationOptions } from '@/hooks/useSpriteAnimation';
import './SpriteAnimator.css';

/** Default optional multiplier (no extra scaling beyond displayScale) */
const DEFAULT_SCALE = 1;

export const SpriteAnimator = memo(function SpriteAnimator({
  entityType,
  entityName,
  animation,
  scale = DEFAULT_SCALE,
  playing = true,
  className,
  onComplete,
  condition,
}: SpriteAnimatorProps) {
  const hookOptions: UseSpriteAnimationOptions = useMemo(() => {
    const opts: UseSpriteAnimationOptions = {
      entityType,
      entityName,
      animation,
      playing,
      condition,
    };
    if (onComplete !== undefined) {
      return { ...opts, onComplete };
    }
    return opts;
  }, [entityType, entityName, animation, playing, onComplete, condition]);

  const { currentFrame, isLoaded, error, animation: anim } = useSpriteAnimation(hookOptions);

  if (error) {
    return (
      <div className={`sprite-animator sprite-animator--error ${className ?? ''}`}>
        <span className="sprite-animator__error-text">⚠</span>
      </div>
    );
  }

  if (!isLoaded || !anim) {
    return (
      <div
        className={`sprite-animator sprite-animator--loading ${className ?? ''}`}
        style={{
          width: 32 * (scale > 1 ? scale : 3),
          height: 48 * (scale > 1 ? scale : 3),
        }}
      />
    );
  }

  // Effective scale = displayScale from config × optional parent multiplier
  const effectiveScale = anim.displayScale * scale;

  // Overlay renderMode — render the actual atlas sprite with blend mode.
  // The generator creates a real PNG atlas (e.g. light_overlay_atlas.png)
  // with one row per time-of-day. We show the correct row via Y offset,
  // and apply mix-blend-mode + opacity from the config.
  if (anim.renderMode === 'overlay') {
    // For overlays: columns=1, so X is always 0.
    // Y offset selects the current row (time-of-day variant).
    const bgOffsetY = -(anim.currentRow * anim.frameHeight);
    const totalHeight = anim.totalRows * anim.frameHeight;

    const style: CSSProperties = {
      width: '100%',
      height: '100%',
      backgroundImage: `url(${anim.atlasUrl})`,
      backgroundSize: `${anim.frameWidth}px ${totalHeight}px`,
      backgroundPosition: `0px ${bgOffsetY}px`,
      backgroundRepeat: 'no-repeat',
      imageRendering: 'pixelated',
      opacity: anim.opacity ?? 0.15,
      mixBlendMode: 'multiply',
      pointerEvents: 'none',
      transition: 'background-position 0.8s ease, opacity 0.8s ease',
    };

    return (
      <div
        className={`sprite-animator sprite-animator--overlay ${className ?? ''}`}
        style={style}
        role="presentation"
        aria-label={`${entityName} ${animation} overlay`}
      />
    );
  }

  // Sprite renderMode (default)
  const displayWidth = anim.frameWidth * effectiveScale;
  const displayHeight = anim.frameHeight * effectiveScale;

  // Background-size: full atlas dimensions scaled
  const bgWidth = anim.frameCount * displayWidth;
  const bgHeight = anim.totalRows * displayHeight;

  // X offset: frame column
  const bgOffsetX = -(currentFrame * displayWidth);
  // Y offset: row (for grid layouts; 0 for strips)
  const bgOffsetY = -(anim.currentRow * displayHeight);

  const style: CSSProperties = {
    width: displayWidth,
    height: displayHeight,
    backgroundImage: `url(${anim.atlasUrl})`,
    backgroundSize: `${bgWidth}px ${bgHeight}px`,
    backgroundPosition: `${bgOffsetX}px ${bgOffsetY}px`,
    backgroundRepeat: 'no-repeat',
    imageRendering: 'pixelated',
  };

  return (
    <div
      className={`sprite-animator ${className ?? ''}`}
      style={style}
      role="img"
      aria-label={`${entityName} ${animation}`}
    />
  );
});

export default SpriteAnimator;
