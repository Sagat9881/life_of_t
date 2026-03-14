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

/**
 * A single row-condition entry from sprite-atlas.json rows[].
 * conditions: predicate map evaluated against GameStateSnapshot.
 * rowIndex: which grid row to use when conditions match.
 * fps / loop: playback overrides for this row.
 */
export interface RowDef {
  rowIndex: number;
  fps?: number;
  loop?: boolean;
  conditions?: SingleCondition[];
}

/**
 * A single predicate condition evaluated against GameStateSnapshot.
 * field: dot-path into snapshot (e.g. "player.energy", "context.timeSlot")
 * op: comparison operator
 * value: right-hand side
 */
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
  /** Multi-row grid: row definitions with predicate conditions */
  rows: RowDef[];
  /** Index of the fallback row when no condition matches */
  defaultRow: number;
}

export interface AtlasConfig {
  entity: string;
  animations: Record<string, AnimationConfig>;
}

export interface SlotState {
  animationName: string;
  frameIndex: number;
  lastFrameTime: number;
  /** Active row index resolved each frame via resolveActiveRow */
  activeRowIndex: number;
}

export interface CanvasAssetsRefs {
  imagesRef: React.MutableRefObject<Map<string, HTMLImageElement>>;
  atlasConfigsRef: React.MutableRefObject<Map<string, AtlasConfig>>;
  slotStateRef: React.MutableRefObject<Map<string, SlotState>>;
}

// ── GameStateSnapshot ────────────────────────────────────────────────────────
// Snapshot of game state for a single render frame.
// Built in GameScreen via useMemo, passed down to canvas pipeline.

export interface GameStateSnapshotPlayer {
  energy: number;
  mood: number;
  hunger: number;
  health: number;
  money: number;
  location: string;
}

export interface GameStateSnapshotContext {
  time: string;      // e.g. "morning", "evening"
  day: number;
  hour: number;
  timeSlot: string;  // raw backend value, e.g. "MORNING"
}

export interface GameStateSnapshotNpcEntry {
  animation: string;
}

export interface GameStateSnapshot {
  player: GameStateSnapshotPlayer;
  context: GameStateSnapshotContext;
  npc: Record<string, GameStateSnapshotNpcEntry>;
}
