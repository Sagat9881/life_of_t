/**
 * SpriteAnimator — renders an animated pixel-art sprite from atlas.
 *
 * ── RELATIVE SCALING SYSTEM ──
 * All sizes are computed relative to the scene viewport (640×480).
 *
 * The parent (LocationRenderer) passes `sceneRelativeHeight` — a fraction
 * of SCENE_HEIGHT that this sprite should occupy. SpriteAnimator converts
 * that into concrete CSS pixel dimensions within the scene-logical space.
 *
 * displayScale from sprite-atlas.json is IGNORED for sizing.
 * The optional `scale` prop is a multiplier ON TOP of the relative size.
 *
 * ── CROP OFFSET ──
 * When atlas frames were cropped to their non-transparent bounding box
 * during generation, the animation carries a `cropOffset` with the
 * original position within the full canvas. SpriteAnimator applies a
 * CSS translate to position the cropped sprite correctly, as if it
 * were still rendered within the full-size canvas.
 *
 * Supports:
 * - strip layout: single-row horizontal atlas (background-position X)
 * - grid layout: multi-row atlas with condition-based row selection
 * - overlay renderMode: atlas image with mix-blend-mode
 */
import { type CSSProperties, memo, useMemo } from 'react';
import type { SpriteAnimatorProps } from '@/types/sprite';
import { useSpriteAnimation } from '@/hooks/useSpriteAnimation';
import type { UseSpriteAnimationOptions } from '@/hooks/useSpriteAnimation';
import { SCENE_HEIGHT } from '@/utils/sceneConstants';
import './SpriteAnimator.css';

const DEFAULT_SCALE = 1;

export const SpriteAnimator = memo(function SpriteAnimator({
  entityType,
  entityName,
  animation,
  scale = DEFAULT_SCALE,
  sceneRelativeHeight,
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
        style={{ width: 64, height: 96 }}
      />
    );
  }

  // ── RELATIVE SCALE COMPUTATION ──
  let displayWidth: number;
  let displayHeight: number;

  if (sceneRelativeHeight !== undefined && sceneRelativeHeight > 0) {
    displayHeight = SCENE_HEIGHT * sceneRelativeHeight * scale;
    const aspectRatio = anim.frameWidth / anim.frameHeight;
    displayWidth = displayHeight * aspectRatio;
  } else {
    displayWidth = anim.frameWidth * scale;
    displayHeight = anim.frameHeight * scale;
  }

  // ── CROP OFFSET ──
  // When frames were cropped, we need to translate the sprite so it
  // appears at its original position within the full canvas.
  // The scale factor maps from cropped frame pixels to display pixels.
  let cropTranslateX = 0;
  let cropTranslateY = 0;
  if (anim.cropOffset) {
    const displayScaleFactor = displayHeight / anim.frameHeight;
    cropTranslateX = anim.cropOffset.x * displayScaleFactor;
    cropTranslateY = anim.cropOffset.y * displayScaleFactor;
  }

  // Overlay renderMode
  if (anim.renderMode === 'overlay') {
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
  const bgWidth = anim.frameCount * displayWidth;
  const bgHeight = anim.totalRows * displayHeight;

  const bgOffsetX = -(currentFrame * displayWidth);
  const bgOffsetY = -(anim.currentRow * displayHeight);

  const style: CSSProperties = {
    width: displayWidth,
    height: displayHeight,
    backgroundImage: `url(${anim.atlasUrl})`,
    backgroundSize: `${bgWidth}px ${bgHeight}px`,
    backgroundPosition: `${bgOffsetX}px ${bgOffsetY}px`,
    backgroundRepeat: 'no-repeat',
    imageRendering: 'pixelated',
    // Apply crop offset as transform so sprite is positioned correctly
    // within its parent container (which is placed at the entity's scene coords).
    ...(cropTranslateX !== 0 || cropTranslateY !== 0
      ? { transform: `translate(${cropTranslateX}px, ${cropTranslateY}px)` }
      : {}),
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
