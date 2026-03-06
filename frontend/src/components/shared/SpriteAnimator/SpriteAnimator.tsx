/**
 * SpriteAnimator — renders an animated pixel-art sprite from a horizontal-strip atlas.
 *
 * Uses CSS background-position to show the current frame of the atlas.
 * The atlas is a single-row PNG where all frames are placed side-by-side.
 *
 * Usage:
 *   <SpriteAnimator
 *     entityType="characters"
 *     entityName="tanya"
 *     animation="idle"
 *     scale={4}
 *   />
 */
import { type CSSProperties, memo, useMemo } from 'react';
import type { SpriteAnimatorProps } from '@/types/sprite';
import { useSpriteAnimation } from '@/hooks/useSpriteAnimation';
import type { UseSpriteAnimationOptions } from '@/hooks/useSpriteAnimation';
import './SpriteAnimator.css';

const DEFAULT_SCALE = 4;

export const SpriteAnimator = memo(function SpriteAnimator({
  entityType,
  entityName,
  animation,
  scale = DEFAULT_SCALE,
  playing = true,
  className,
  onComplete,
}: SpriteAnimatorProps) {
  const hookOptions: UseSpriteAnimationOptions = useMemo(() => {
    const opts: UseSpriteAnimationOptions = {
      entityType,
      entityName,
      animation,
      playing,
    };
    if (onComplete !== undefined) {
      return { ...opts, onComplete };
    }
    return opts;
  }, [entityType, entityName, animation, playing, onComplete]);

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
          width: 16 * scale,
          height: 16 * scale,
        }}
      />
    );
  }

  const displayWidth = anim.frameWidth * scale;
  const displayHeight = anim.frameHeight * scale;
  const bgWidth = anim.frameCount * displayWidth;
  const bgOffsetX = -(currentFrame * displayWidth);

  const style: CSSProperties = {
    width: displayWidth,
    height: displayHeight,
    backgroundImage: `url(${anim.atlasUrl})`,
    backgroundSize: `${bgWidth}px ${displayHeight}px`,
    backgroundPosition: `${bgOffsetX}px 0px`,
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
