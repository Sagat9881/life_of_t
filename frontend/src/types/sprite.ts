/**
 * Types for SpriteAnimator component.
 * Kept minimal — only what SpriteAnimator.tsx actually uses.
 */
export interface SpriteAnimatorProps {
  entityType:           string;
  entityName:           string;
  animation:            string;
  scale?:               number;
  /** Fraction of SCENE_HEIGHT this sprite should occupy (0–1) */
  sceneRelativeHeight?: number;
  playing?:             boolean;
  className?:           string;
  onComplete?:          () => void;
  /** Row-selector for grid-layout atlases */
  condition?:           string;
}
