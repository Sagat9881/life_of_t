/**
 * atlasUtils — pure functions for multi-row sprite-atlas resolution.
 *
 * resolveActiveRow: evaluates RowDef[] conditions against GameStateSnapshot,
 *                   returns the index of the first matching row, or defaultRow.
 *
 * getRowPlayback:   returns { fps, loop } for the active row, falling back
 *                   to the defaultRow's values or safe defaults.
 */

import type { RowDef, SingleCondition, GameStateSnapshot } from './canvasTypes';

// ── helpers ──────────────────────────────────────────────────────────────────

/** Resolve a dot-path field from a plain object. Returns undefined if not found. */
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
  // snapshot is treated as Record<string, unknown> for field traversal
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

// ── public API ────────────────────────────────────────────────────────────────

/**
 * Evaluate rows[] conditions against snapshot.
 * Returns rowIndex of first matching RowDef, or defaultRow if none match.
 */
export function resolveActiveRow(
  rows: RowDef[],
  defaultRow: number,
  snapshot: GameStateSnapshot,
): number {
  if (!rows || rows.length === 0) return defaultRow;

  for (const row of rows) {
    if (rowMatches(row, snapshot)) {
      return row.rowIndex;
    }
  }

  return defaultRow;
}

/**
 * Returns playback params { fps, loop } for the active row.
 * Falls back to defaultRow's params, then to safe defaults.
 */
export function getRowPlayback(
  rows: RowDef[],
  activeRowIndex: number,
  defaultRow: number,
): { fps: number; loop: boolean } {
  if (!rows || rows.length === 0) return { fps: 8, loop: true };

  const activeRowDef = rows.find((r) => r.rowIndex === activeRowIndex);
  if (activeRowDef?.fps !== undefined) {
    return {
      fps:  activeRowDef.fps,
      loop: activeRowDef.loop ?? true,
    };
  }

  const defaultRowDef = rows.find((r) => r.rowIndex === defaultRow);
  return {
    fps:  defaultRowDef?.fps  ?? 8,
    loop: defaultRowDef?.loop ?? true,
  };
}
