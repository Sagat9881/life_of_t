/**
 * Asset loading service.
 * Loads sprite-atlas.json (v1.1) and resolves animations.
 */
import { ASSETS_BASE_URL } from '@/utils/constants';
import type { AtlasConfig, AtlasAnimationEntry, SpriteAnimation } from '@/types/sprite';

const configCache = new Map<string, AtlasConfig>();

export const getEntityBaseUrl = (entityType: string, entityName: string): string => {
  return `${ASSETS_BASE_URL}/${entityType}/${entityName}`;
};

export const getCompositeUrl = (entityType: string, entityName: string): string => {
  return `${getEntityBaseUrl(entityType, entityName)}/${entityName}.png`;
};

/**
 * Loads and caches sprite-atlas.json (v1.1 object format) for an entity.
 */
export const loadAtlasConfig = async (
  entityType: string,
  entityName: string
): Promise<AtlasConfig> => {
  const cacheKey = `${entityType}/${entityName}`;
  const cached = configCache.get(cacheKey);
  if (cached) return cached;

  const url = `${getEntityBaseUrl(entityType, entityName)}/animations/sprite-atlas.json?v=latest`;
  const response = await fetch(url);

  if (!response.ok) {
    throw new Error(`Failed to load atlas config: ${url} (${response.status})`);
  }

  const config = (await response.json()) as AtlasConfig;

  // Validate config version (must be 1.*)
  const major = parseInt(config.configVersion?.split('.')[0] ?? '0', 10);
  if (major !== 1) {
    throw new Error(
      `Unsupported atlas config version: ${config.configVersion}. Expected 1.*`
    );
  }

  configCache.set(cacheKey, config);
  return config;
};

/**
 * Resolves a specific animation from sprite-atlas.json into a renderable SpriteAnimation.
 * Supports both strip and grid layouts.
 *
 * @param condition - current condition value for grid row selection (e.g. "morning")
 */
export const resolveAnimation = (
  entityType: string,
  entityName: string,
  animationName: string,
  config: AtlasConfig,
  condition?: string
): SpriteAnimation | null => {
  const entry: AtlasAnimationEntry | undefined = config.animations[animationName];
  if (!entry) return null;

  const baseUrl = getEntityBaseUrl(entityType, entityName);
  const atlasUrl = `${baseUrl}/animations/${entry.file}`;

  if (entry.layout === 'grid' && entry.rows && entry.rows.length > 0) {
    // Grid layout: find the row matching the condition
    let row = entry.rows.find(
      (r) => condition && r.condition.value === condition
    );
    // Fallback to default row
    if (!row) {
      const defaultIdx = entry.defaultRow ?? 0;
      row = entry.rows.find((r) => r.rowIndex === defaultIdx) ?? entry.rows[0];
    }

    return {
      name: animationName,
      atlasUrl,
      frameWidth: entry.frameWidth,
      frameHeight: entry.frameHeight,
      frameCount: entry.columns,
      fps: row!.fps,
      loop: row!.loop,
      layout: 'grid',
      totalRows: entry.rows.length,
      currentRow: row!.rowIndex,
    };
  }

  // Strip layout (default)
  return {
    name: animationName,
    atlasUrl,
    frameWidth: entry.frameWidth,
    frameHeight: entry.frameHeight,
    frameCount: entry.columns,
    fps: entry.fps ?? 8,
    loop: entry.loop ?? true,
    layout: 'strip',
    totalRows: 1,
    currentRow: 0,
  };
};

export const preloadAtlasImage = (url: string): Promise<HTMLImageElement> => {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.onload = () => resolve(img);
    img.onerror = () => reject(new Error(`Failed to preload atlas: ${url}`));
    img.src = url;
  });
};

export const clearAssetCache = (): void => {
  configCache.clear();
};
