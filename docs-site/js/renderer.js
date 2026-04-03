/**
 * renderer.js — render layer
 * createCard(entity): builds a DOM card from entity object fields.
 * ADR-001: operates only on object properties — no hardcoded names or IDs.
 */

/**
 * @param {object} entity — one item from docs-preview.json
 * @returns {HTMLElement}
 */
export function createCard(entity) {
  const card = document.createElement('article');
  card.className = 'entity-card';
  card.setAttribute('role', 'listitem');
  card.setAttribute('tabindex', '0');
  card.setAttribute('aria-label', entity.displayName);

  // Sprite preview
  const spriteWrap = document.createElement('div');
  spriteWrap.className = 'card-sprite';

  if (entity.spriteAtlasFile) {
    const img = document.createElement('img');
    img.src = `./assets/${entity.spriteAtlasFile}`;
    img.alt = entity.displayName;
    img.width = 64;
    img.height = 64;
    img.loading = 'lazy';
    img.decoding = 'async';
    img.onerror = () => {
      img.replaceWith(buildPlaceholderIcon(entity.displayName));
    };
    spriteWrap.appendChild(img);
  } else {
    spriteWrap.appendChild(buildPlaceholderIcon(entity.displayName));
  }

  card.appendChild(spriteWrap);

  // Card body
  const body = document.createElement('div');
  body.className = 'card-body';

  const title = document.createElement('h2');
  title.className = 'card-title';
  title.textContent = entity.displayName;
  body.appendChild(title);

  const badge = document.createElement('span');
  badge.className = 'card-badge';
  badge.textContent = entity.type;
  body.appendChild(badge);

  // Animations list (if any)
  if (Array.isArray(entity.animations) && entity.animations.length > 0) {
    const animWrap = document.createElement('div');
    animWrap.className = 'card-animations';
    for (const anim of entity.animations) {
      const chip = document.createElement('span');
      chip.className = 'anim-chip';
      chip.textContent = anim;
      animWrap.appendChild(chip);
    }
    body.appendChild(animWrap);
  }

  // Color palette swatches
  if (Array.isArray(entity.colorPalette) && entity.colorPalette.length > 0) {
    const palette = document.createElement('div');
    palette.className = 'card-palette';
    palette.setAttribute('aria-label', 'Color palette');
    for (const color of entity.colorPalette) {
      const swatch = document.createElement('span');
      swatch.className = 'color-swatch';
      swatch.style.backgroundColor = color.hex;
      swatch.title = color.name;
      swatch.setAttribute('aria-label', `${color.name}: ${color.hex}`);
      palette.appendChild(swatch);
    }
    body.appendChild(palette);
  }

  card.appendChild(body);
  return card;
}

/**
 * Builds a placeholder icon when spriteAtlasFile is null or image fails to load.
 * @param {string} displayName
 * @returns {HTMLElement}
 */
function buildPlaceholderIcon(displayName) {
  const wrap = document.createElement('div');
  wrap.className = 'sprite-placeholder';
  wrap.setAttribute('role', 'img');
  wrap.setAttribute('aria-label', `${displayName} — no sprite`);
  // Show first letter as fallback monogram
  wrap.textContent = displayName ? displayName.charAt(0).toUpperCase() : '?';
  return wrap;
}
