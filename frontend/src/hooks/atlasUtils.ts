/**
 * atlasUtils — pure functions for multi-row sprite-atlas resolution.
 */

import type { RowDef, SingleCondition, GameStateSnapshot } from './canvasTypes';
import { DEFAULT_ANIMATION_FPS } from './canvasTypes';

function getField(obj: Record<string, unknown>, path: string): unknown {
  const parts = path.split('.');
  let cursor: unknown = obj;
  for (const part of parts) {
    if (cursor === null || cursor === undefined || typeof cursor !== 'object') return undefined;
    cursor = (cursor as Record<string, unknown>)[part];
  }
  return cursor;
}

function evalCondition(condition: SingleCondition, snapshot: GameStateSnapshot): boolean {
  const raw = getField(snapshot as unknown as Record<string, unknown>, condition.field);
  if (raw === undefined || raw === null) return false;
  const { op, value } = condition;
  switch (op) {
    case 'eq':  return raw === value;
    case 'neq': return raw !== value;
    case 'lt':  return typeof raw === 'number' && typeof value === 'number' && raw < value;
    case 'lte': return typeof raw === 'number' && typeof value === 'number' && raw <= value;
    case 'gt':  return typeof raw === 'number' && typeof value === 'number' && raw > value;
    case 'gte': return typeof raw === 'number' && typeof value === 'number' && raw >= value;
    default:    return false;
  }
}

function rowMatches(row: RowDef, snapshot: GameStateSnapshot): boolean {
  if (!row.conditions || row.conditions.length === 0) return true;
  return row.conditions.every((c) => evalCondition(c, snapshot));
}

export function resolveActiveRow(
  rows: RowDef[],
  defaultRow: number,
  snapshot: GameStateSnapshot,
): number {
  if (!rows || rows.length === 0) return defaultRow;
  for (const row of rows) {
    if (rowMatches(row, snapshot)) return row.rowIndex;
  }
  return defaultRow;
}

export function getRowPlayback(
  rows: RowDef[],
  activeRowIndex: number,
  defaultRow: number,
): { fps: number; loop: boolean } {
  if (!rows || rows.length === 0) return { fps: DEFAULT_ANIMATION_FPS, loop: true };

  const activeRowDef = rows.find((r) => r.rowIndex === activeRowIndex);
  if (activeRowDef?.fps !== undefined) {
    return { fps: activeRowDef.fps, loop: activeRowDef.loop ?? true };
  }

  const defaultRowDef = rows.find((r) => r.rowIndex === defaultRow);
  return {
    fps:  defaultRowDef?.fps  ?? DEFAULT_ANIMATION_FPS,
    loop: defaultRowDef?.loop ?? true,
  };
}
