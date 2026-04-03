/**
 * filter.js — state layer
 * Pure functions for filtering the entities array.
 * ADR-001: uses only object fields, no hardcoded entity identifiers.
 */

/**
 * Returns entities matching the given type.
 * Pass 'all' to skip filtering.
 * @param {object[]} entities
 * @param {string} type
 * @returns {object[]}
 */
export function filterByType(entities, type) {
  if (!type || type === 'all') return entities;
  return entities.filter((e) => e.type === type);
}

/**
 * Returns entities whose displayName contains the query (case-insensitive).
 * @param {object[]} entities
 * @param {string} query
 * @returns {object[]}
 */
export function filterByName(entities, query) {
  if (!query) return entities;
  const lower = query.toLowerCase();
  return entities.filter((e) =>
    typeof e.displayName === 'string' && e.displayName.toLowerCase().includes(lower)
  );
}
