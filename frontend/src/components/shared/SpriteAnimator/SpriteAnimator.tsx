/**
 * SpriteAnimator — renders an animated pixel-art sprite from atlas.
 *
 * Supports:
 * - strip layout: single-row horizontal atlas (background-position X)
 * - grid layout: multi-row atlas (background-position X + Y) with condition-based row selection
 *
 * Usage:
 *   <SpriteAnimator entityType="characters" entityName="tanya" animation="idle" scale={5} condition="morning" />
 */
import { type CSSProperties, memo, useMemo } from 'react';
import type { SpriteAnimatorProps } from '@/types/sprite';
import { useSpriteAnimation } from '@/hooks/useSpriteAnimation';
import type { UseSpriteAnimationOptions } from '@/hooks/useSpriteAnimation';
import './SpriteAnimator.css';

/** Default scale for sprites in the native PixelScene coordinate system (480×270) */
const DEFAULT_SCALE = 5;

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
          width: 32 * scale,
          height: 48 * scale,
        }}
      />
    );
  }

  const displayWidth = anim.frameWidth * scale;
  const displayHeight = anim.frameHeight * scale;

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
