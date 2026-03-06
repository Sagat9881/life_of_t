/**
 * Asset loading service.
 * Resolves paths to generated assets served by Spring Boot from /assets/.
 */
import { ASSETS_BASE_URL } from '@/utils/constants';
import type { AtlasConfig, AtlasAnimationEntry, SpriteAnimation } from '@/types/sprite';

/** Cache for loaded atlas configs to avoid redundant fetches */
const configCache = new Map<string, AtlasConfig>();

/**
 * Builds the base URL for an entity's assets directory.
 * Example: getEntityBaseUrl('characters', 'tanya') => '/assets/characters/tanya'
 */
export const getEntityBaseUrl = (entityType: string, entityName: string): string => {
  return `${ASSETS_BASE_URL}/${entityType}/${entityName}`;
};

/**
 * Builds the URL for the entity's composite static image.
 * Example: getCompositeUrl('characters', 'tanya') => '/assets/characters/tanya/tanya.png'
 */
export const getCompositeUrl = (entityType: string, entityName: string): string => {
  return `${getEntityBaseUrl(entityType, entityName)}/${entityName}.png`;
};

/**
 * Loads and caches the combined atlas-config.json for an entity.
 */
export const loadAtlasConfig = async (
  entityType: string,
  entityName: string
): Promise<AtlasConfig> => {
  const cacheKey = `${entityType}/${entityName}`;
  const cached = configCache.get(cacheKey);
  if (cached) return cached;

  const url = `${getEntityBaseUrl(entityType, entityName)}/animations/atlas-config.json`;
  const response = await fetch(url);

  if (!response.ok) {
    throw new Error(`Failed to load atlas config: ${url} (${response.status})`);
  }

  const config: AtlasConfig = await response.json() as AtlasConfig;
  configCache.set(cacheKey, config);
  return config;
};

/**
 * Resolves a specific animation from the atlas config into a renderable SpriteAnimation.
 */
export const resolveAnimation = (
  entityType: string,
  entityName: string,
  animationName: string,
  config: AtlasConfig
): SpriteAnimation | null => {
  const entry: AtlasAnimationEntry | undefined = config.find(
    (e) => e.name === animationName
  );
  if (!entry) return null;

  const atlasFileName = entry.file ?? `${animationName}_atlas.png`;
  const baseUrl = getEntityBaseUrl(entityType, entityName);

  return {
    name: entry.name,
    atlasUrl: `${baseUrl}/animations/${atlasFileName}`,
    frameWidth: entry.frameWidth,
    frameHeight: entry.frameHeight,
    frameCount: entry.frames,
    fps: entry.fps,
    loop: entry.loop,
  };
};

/**
 * Preloads an atlas image into browser cache.
 * Returns a promise that resolves when the image is loaded.
 */
export const preloadAtlasImage = (url: string): Promise<HTMLImageElement> => {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.onload = () => resolve(img);
    img.onerror = () => reject(new Error(`Failed to preload atlas: ${url}`));
    img.src = url;
  });
};

/**
 * Clears the atlas config cache (useful for hot reload / dev).
 */
export const clearAssetCache = (): void => {
  configCache.clear();
};
