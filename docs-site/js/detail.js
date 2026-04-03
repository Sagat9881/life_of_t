/**
 * detail.js — detail panel
 * Opens/closes the entity detail panel with sprite animation placeholder
 * and pretty-printed JSON spec.
 * ADR-001: no hardcoded entity IDs or names.
 */

const panel = document.getElementById('detail-panel');
const backdrop = document.getElementById('detail-backdrop');
const closeBtn = document.getElementById('detail-close');
const detailBody = document.getElementById('detail-body');

export function initDetailPanel() {
  closeBtn.addEventListener('click', closeDetail);
  backdrop.addEventListener('click', closeDetail);
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') closeDetail();
  });
}

/**
 * Opens the detail panel for the given entity.
 * @param {object} entity
 */
export function openDetail(entity) {
  detailBody.innerHTML = '';

  // Header
  const header = document.createElement('div');
  header.className = 'detail-header';

  const title = document.createElement('h2');
  title.id = 'detail-title';
  title.textContent = entity.displayName;
  header.appendChild(title);

  const badge = document.createElement('span');
  badge.className = 'card-badge';
  badge.textContent = entity.type;
  header.appendChild(badge);

  detailBody.appendChild(header);

  // Sprite area
  const spriteArea = document.createElement('div');
  spriteArea.className = 'detail-sprite-area';

  if (entity.spriteAtlasFile) {
    const img = document.createElement('img');
    img.src = `./assets/${entity.spriteAtlasFile}`;
    img.alt = entity.displayName;
    img.className = 'detail-sprite-img';
    img.onerror = () => img.replaceWith(buildDetailPlaceholder(entity.displayName));
    spriteArea.appendChild(img);
  } else {
    spriteArea.appendChild(buildDetailPlaceholder(entity.displayName));
  }

  detailBody.appendChild(spriteArea);

  // JSON spec
  const specSection = document.createElement('details');
  specSection.className = 'detail-spec';
  specSection.open = false;

  const summary = document.createElement('summary');
  summary.textContent = 'JSON Specification';
  specSection.appendChild(summary);

  const pre = document.createElement('pre');
  pre.className = 'detail-json';
  pre.textContent = JSON.stringify(entity, null, 2);
  specSection.appendChild(pre);

  detailBody.appendChild(specSection);

  panel.hidden = false;
  panel.focus();
}

export function closeDetail() {
  panel.hidden = true;
  detailBody.innerHTML = '';
}

function buildDetailPlaceholder(displayName) {
  const el = document.createElement('div');
  el.className = 'detail-sprite-placeholder';
  el.setAttribute('role', 'img');
  el.setAttribute('aria-label', `${displayName} — no sprite`);
  el.textContent = displayName ? displayName.charAt(0).toUpperCase() : '?';
  return el;
}
