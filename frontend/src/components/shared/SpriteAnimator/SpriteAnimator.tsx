/**
 * SpriteAnimator — renders an animated pixel-art sprite from atlas.
 *
 * Supports:
 * - strip layout: single-row horizontal atlas (background-position X)
 * - grid layout: multi-row atlas (background-position X + Y) with condition-based row selection
 * - overlay renderMode: renders as a colored overlay (tint + opacity + blend mode)
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

  // Overlay renderMode — render as a colored div, not a sprite sheet
  if (anim.renderMode === 'overlay') {
    const style: CSSProperties = {
      width: '100%',
      height: '100%',
      backgroundColor: anim.tint ?? '#000000',
      opacity: anim.opacity ?? 0.1,
      mixBlendMode: 'multiply',
      pointerEvents: 'none',
      transition: 'background-color 1s ease, opacity 1s ease',
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
