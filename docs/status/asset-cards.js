/**
 * asset-cards.js — TASK-FE-053
 * Компонент .asset-card с hover-preview анимации
 * Vanilla ES2020, без фреймворков.
 */

'use strict';

// ── Helpers ──────────────────────────────────────────────────────────────────
function escHtml(s) {
  return String(s ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g,  '&lt;')
    .replace(/>/g,  '&gt;')
    .replace(/"/g,  '&quot;');
}

function formatBytes(bytes) {
  if (bytes == null) return '—';
  const n = Number(bytes);
  if (!isFinite(n) || n < 0) return '—';
  if (n === 0) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log2(n) / 10);
  const idx = Math.min(i, units.length - 1);
  return `${(n / Math.pow(1024, idx)).toFixed(idx === 0 ? 0 : 1)} ${units[idx]}`;
}

function formatDims(w, h) {
  if (w == null || h == null) return '—';
  return `${w}×${h}`;
}

// ── Placeholder inline SVG (base64 data URI) ──────────────────────────────────
const PLACEHOLDER_SVG = (() => {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 64 64">
    <rect width="64" height="64" fill="#2e2c25"/>
    <line x1="4" y1="4"  x2="60" y2="60" stroke="#5a5850" stroke-width="2"/>
    <line x1="60" y1="4" x2="4"  y2="60" stroke="#5a5850" stroke-width="2"/>
    <rect x="2" y="2" width="60" height="60" fill="none" stroke="#3a3830" stroke-width="2"/>
  </svg>`;
  return 'data:image/svg+xml;base64,' + btoa(svg);
})();

// ── Status badge helper ───────────────────────────────────────────────────────
function statusClass(status) {
  const map = { ok: 'good', generated: 'good', error: 'error', missing: 'error',
                warn: 'warn', warning: 'warn', pending: 'info', unknown: 'info' };
  return map[String(status).toLowerCase()] ?? 'info';
}

// ── Preview tooltip position ──────────────────────────────────────────────────
function positionPreview(card, preview) {
  const rect  = card.getBoundingClientRect();
  const vw    = window.innerWidth;
  const pw    = 200; // preview width
  const gap   = 8;

  // prefer right, then left, then above card
  if (rect.right + pw + gap < vw) {
    preview.style.left = `calc(100% + ${gap}px)`;
    preview.style.right = '';
    preview.style.top   = '0';
  } else if (rect.left - pw - gap > 0) {
    preview.style.right = `calc(100% + ${gap}px)`;
    preview.style.left  = '';
    preview.style.top   = '0';
  } else {
    preview.style.left  = '0';
    preview.style.right = '';
    preview.style.top   = `calc(100% + ${gap}px)`;
  }
}

// ── Build a single .asset-card element ───────────────────────────────────────
/**
 * @param {Object} data
 * @param {string}  data.id          — уникальный идентификатор
 * @param {string}  data.name        — имя файла / ключ атласа
 * @param {string}  [data.src]       — URL изображения атласа
 * @param {string}  [data.type]      — тип: «atlas» | «sprite» | «tile» | …
 * @param {string}  [data.status]    — «ok» | «error» | «warn» | «pending» | …
 * @param {number|null} [data.fileSizeBytes] — размер файла, null → «—»
 * @param {number}  [data.width]     — ширина в пикселях
 * @param {number}  [data.height]    — высота в пикселях
 * @param {number}  [data.sprites]   — кол-во спрайтов в атласе
 * @param {string}  [data.updatedAt] — дата обновления (ISO)
 * @param {boolean} [data.noMeta]    — true → отображается предупреждение §4.1
 * @returns {HTMLElement}
 */
export function buildAssetCard(data) {
  const {
    id       = '',
    name     = 'unknown',
    src      = null,
    type     = 'asset',
    status   = 'unknown',
    fileSizeBytes = null,
    width    = null,
    height   = null,
    sprites  = null,
    updatedAt = null,
    noMeta   = false,
  } = data ?? {};

  const st       = statusClass(status);
  const sizeStr  = formatBytes(fileSizeBytes);
  const dimsStr  = formatDims(width, height);
  const imgSrc   = src || PLACEHOLDER_SVG;
  const isBroken = !src;

  // ── Card root (§4.1 structure) ────────────────────────────────────────────
  const card = document.createElement('article');
  card.className = 'asset-card' + (isBroken ? ' broken' : '') + (noMeta ? ' no-meta' : '');
  card.dataset.assetId = escHtml(id);
  card.setAttribute('tabindex', '0');
  card.setAttribute('aria-label',
    `Ассет ${escHtml(name)}, тип ${escHtml(type)}, статус ${escHtml(status)}`);

  // Thumbnail ──────────────────────────────────────────────────────────────
  const thumb = document.createElement('div');
  thumb.className = 'asset-thumb';

  const img = document.createElement('img');
  img.src     = imgSrc;
  img.alt     = escHtml(name);
  img.width   = 64;
  img.height  = 64;
  img.loading = 'lazy';
  img.decoding = 'async';
  if (isBroken) img.classList.add('broken-img');

  // Broken fallback: если src указан но изображение не загрузится
  if (src) {
    img.addEventListener('error', () => {
      img.src = PLACEHOLDER_SVG;
      card.classList.add('broken');
      img.classList.add('broken-img');
    }, { once: true });
  }

  // ── Hover Preview (§7.2, §7.3) ──────────────────────────────────────────
  const preview = document.createElement('div');
  preview.className = 'asset-preview';
  preview.setAttribute('aria-hidden', 'true');

  const previewImg = document.createElement('img');
  previewImg.src    = imgSrc;
  previewImg.alt    = '';
  previewImg.width  = 200;
  previewImg.height = 200;
  previewImg.decoding = 'async';
  previewImg.style.imageRendering = 'pixelated';

  // Keep preview src in sync with fallback
  if (src) {
    img.addEventListener('error', () => { previewImg.src = PLACEHOLDER_SVG; }, { once: true });
  }

  const previewLabel = document.createElement('div');
  previewLabel.className = 'asset-preview-label';
  previewLabel.textContent = name;

  preview.appendChild(previewImg);
  preview.appendChild(previewLabel);

  thumb.appendChild(img);
  thumb.appendChild(preview);

  // Position preview on mouse enter
  const showPreview = () => positionPreview(card, preview);
  card.addEventListener('mouseenter', showPreview);
  card.addEventListener('focusin',    showPreview);

  // ── Info block ────────────────────────────────────────────────────────────
  const info = document.createElement('div');
  info.className = 'asset-info';

  const nameEl = document.createElement('div');
  nameEl.className = 'asset-name';
  nameEl.textContent = name;
  nameEl.title = name;

  // Badges row
  const badges = document.createElement('div');
  badges.className = 'asset-badges';

  const typeBadge   = document.createElement('span');
  typeBadge.className = 'asset-badge info';
  typeBadge.textContent = type.toUpperCase();

  const statusBadge = document.createElement('span');
  statusBadge.className = `asset-badge ${st}`;
  statusBadge.textContent = status.toUpperCase();

  badges.appendChild(typeBadge);
  badges.appendChild(statusBadge);

  // Meta rows
  const meta = document.createElement('dl');
  meta.className = 'asset-meta';

  const metaRows = [
    { label: 'Размер',    value: sizeStr },
    { label: 'Размеры',   value: dimsStr },
    { label: 'Спрайты',   value: sprites  != null ? String(sprites) : '—' },
    { label: 'Обновлено', value: updatedAt ? new Date(updatedAt).toLocaleDateString('ru-RU') : '—' },
  ];
  metaRows.forEach(({ label, value }) => {
    const dt = document.createElement('dt');
    dt.textContent = label;
    const dd = document.createElement('dd');
    dd.textContent = value;
    if (value === '—') dd.classList.add('meta-empty');
    meta.appendChild(dt);
    meta.appendChild(dd);
  });

  info.appendChild(nameEl);
  info.appendChild(badges);
  info.appendChild(meta);

  // ── No-meta warning (§4.1) ───────────────────────────────────────────────
  if (noMeta) {
    const warn = document.createElement('div');
    warn.className = 'asset-no-meta-warn';
    warn.setAttribute('role', 'alert');
    warn.innerHTML = '⚠&#xFE0E; <span>Метаданные отсутствуют</span>';
    info.appendChild(warn);
  }

  card.appendChild(thumb);
  card.appendChild(info);

  return card;
}

// ── Init a grid of cards ──────────────────────────────────────────────────────
/**
 * @param {string|HTMLElement} container  — id или DOM-элемент
 * @param {Object[]}           assets     — массив данных карточек
 */
export function initAssetGrid(container, assets) {
  const el = typeof container === 'string'
    ? document.getElementById(container)
    : container;
  if (!el) return;

  el.innerHTML = '';
  el.classList.add('asset-grid');

  if (!assets?.length) {
    const empty = document.createElement('div');
    empty.className = 'asset-empty';
    empty.innerHTML = '<span class="asset-empty-icon">◻</span><p>Нет ассетов</p>';
    el.appendChild(empty);
    return;
  }

  assets.forEach(data => el.appendChild(buildAssetCard(data)));
}
