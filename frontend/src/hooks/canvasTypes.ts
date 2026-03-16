/**
 * Shared types for Canvas pipeline hooks.
 * Extracted from the original useCanvasRenderer monolith.
 * Do NOT import from this file in non-canvas code.
 */

import React from 'react';

export interface CropOffset {
  x: number;
  y: number;
  originalWidth: number;
  originalHeight: number;
}

export interface RowDef {
  rowIndex: number;
  fps?: number;
  loop?: boolean;
  conditions?: SingleCondition[];
}

export interface SingleCondition {
  field: string;
  op: 'lt' | 'lte' | 'gt' | 'gte' | 'eq' | 'neq';
  value: number | string | boolean;
}

export interface AnimationConfig {
  file: string;
  layout: 'strip' | 'grid';
  columns: number;
  frameWidth: number;
  frameHeight: number;
  fps: number;
  loop: boolean;
  cropOffset?: CropOffset;
  rows: RowDef[];
  defaultRow: number;
}

export interface AtlasConfig {
  entity: string;
  animations: Record<string, AnimationConfig>;
}

// ── Canvas pipeline constants ─────────────────────────────────────────────────────

/** Fallback background color rendered before any atlas loads. */
export const SCENE_FALLBACK_COLOR = '#1a1a2e';

/** SlotState key reserved for the background layer. */
export const BACKGROUND_SLOT_KEY = '__background__';

/** Slot key prefix for furniture entities. */
export const FURNITURE_SLOT_PREFIX = 'furniture_';

/** Default animation FPS when sprite-atlas.json provides no fps value. */
export const DEFAULT_ANIMATION_FPS = 8;

/**
 * Discriminates slot categories in the render loop.
 * Extend here when adding new entity categories (pet, ui-element, etc.).
 */
export type SlotKind = 'character' | 'furniture' | 'background';

export function getSlotKind(slotId: string): SlotKind {
  if (slotId === BACKGROUND_SLOT_KEY)           return 'background';
  if (slotId.startsWith(FURNITURE_SLOT_PREFIX)) return 'furniture';
  return 'character';
}

// ── SlotState ─────────────────────────────────────────────────────────────────────

export interface SlotState {
  animationName: string;
  frameIndex: number;
  lastFrameTime: number;
  activeRowIndex: number;
  kind: SlotKind;
}

export interface CanvasAssetsRefs {
  imagesRef: React.MutableRefObject<Map<string, HTMLImageElement>>;
  atlasConfigsRef: React.MutableRefObject<Map<string, AtlasConfig>>;
  slotStateRef: React.MutableRefObject<Map<string, SlotState>>;
}

// ── GameStateSnapshot ───────────────────────────────────────────────────────────

export interface GameStateSnapshotPlayer {
  energy: number;
  mood: number;
  hunger: number;
  health: number;
  money: number;
  stress: number;
  selfEsteem: number;
  location: string;
  tags: Record<string, boolean>;
}

export interface GameStateSnapshotContext {
  time: string;
  day: number;
  hour: number;
  timeSlot: string;
  dayOfWeek: number;
}

export interface GameStateSnapshotNpcEntry {
  animation: string;
}

export interface GameStateSnapshotRelationship {
  closeness: number;
  trust: number;
  stability: number;
  romance: number;
}

export interface GameStateSnapshot {
  player: GameStateSnapshotPlayer;
  context: GameStateSnapshotContext;
  npc: Record<string, GameStateSnapshotNpcEntry>;
  relationships: Record<string, GameStateSnapshotRelationship>;
}
