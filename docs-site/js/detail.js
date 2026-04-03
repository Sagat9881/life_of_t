/**
 * detail.js — detail panel: state + render + sprite animation
 *
 * Layers:
 *   state  — detailState, openDetail(), closeDetail()
 *   render — renderSprite(), renderAnimControls(), renderPalette(),
 *             renderAnimList(), renderJson()
 *
 * ADR-001: operates ONLY on entity object fields.
 *          No hardcoded entity IDs, names, or switch-case by entity.id.
 */

// ── State ──────────────────────────────────────────────────────────────────

/** @type {{ entity: object|null, isOpen: boolean, rafId: number|null, frameIndex: number, isPlaying: boolean, image: HTMLImageElement|null, frameCount: number, frameW: number, frameH: number, fps: number, lastFrameTime: number }} */
const detailState = {
  entity: null,
  isOpen: false,
  rafId: null,
  frameIndex: 0,
  isPlaying: false,
  image: null,
  frameCount: 0,
  frameW: 0,
  frameH: 0,
  fps: 6,
  lastFrameTime: 0,
};

// ── DOM refs ───────────────────────────────────────────────────────────────

const panel = document.getElementById('detail-panel');
const backdrop = document.getElementById('detail-backdrop');
const closeBtn = document.getElementById('detail-close');
const detailBody = document.getElementById('detail-body');

// ── Init ───────────────────────────────────────────────────────────────────

export function initDetailPanel() {
  closeBtn.addEventListener('click', closeDetail);
  backdrop.addEventListener('click', closeDetail);
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && detailState.isOpen) closeDetail();
  });
}

// ── Public API ─────────────────────────────────────────────────────────────

/**
 * Opens the detail panel for the given entity.
 * @param {object} entity — one item from docs-preview.json
 */
export function openDetail(entity) {
  stopAnimation();
  resetState(entity);

  detailBody.innerHTML = '';
  detailBody.appendChild(buildHeader(entity));
  detailBody.appendChild(buildSpriteSection(entity));

  if (Array.isArray(entity.animations) && entity.animations.length > 0) {
    detailBody.appendChild(buildAnimList(entity.animations));
  }

  if (Array.isArray(entity.colorPalette) && entity.colorPalette.length > 0) {
    detailBody.appendChild(buildPaletteSection(entity.colorPalette));
  }

  detailBody.appendChild(buildJsonSection(entity));

  panel.hidden = false;
  detailState.isOpen = true;
  panel.focus();
}

/**
 * Closes the detail panel and cancels any running animation.
 */
export function closeDetail() {
  stopAnimation();
  panel.hidden = true;
  detailState.isOpen = false;
  detailState.entity = null;
  detailBody.innerHTML = '';
}

// ── State helpers ───────────────────────────────────────────────────────────

function resetState(entity) {
  detailState.entity = entity;
  detailState.frameIndex = 0;
  detailState.isPlaying = false;
  detailState.image = null;
  detailState.frameCount = 0;
  detailState.frameW = 0;
  detailState.frameH = 0;
  detailState.lastFrameTime = 0;
  detailState.rafId = null;
}

function stopAnimation() {
  if (detailState.rafId !== null) {
    cancelAnimationFrame(detailState.rafId);
    detailState.rafId = null;
  }
  detailState.isPlaying = false;
}

// ── Builders ──────────────────────────────────────────────────────────────────

function buildHeader(entity) {
  const header = document.createElement('div');
  header.className = 'detail-header';

  const title = document.createElement('h2');
  title.id = 'detail-title';
  title.textContent = entity.displayName;

  const badge = document.createElement('span');
  badge.className = 'card-badge';
  badge.textContent = entity.type;

  header.appendChild(title);
  header.appendChild(badge);
  return header;
}

/**
 * Builds the sprite area:
 * - If spriteAtlasFile is set: canvas + controls
 * - If null: monogram placeholder
 */
function buildSpriteSection(entity) {
  const section = document.createElement('div');
  section.className = 'detail-sprite-section';

  if (!entity.spriteAtlasFile) {
    section.appendChild(buildPlaceholder(entity.displayName));
    return section;
  }

  const canvasWrap = document.createElement('div');
  canvasWrap.className = 'detail-sprite-area';

  const canvas = document.createElement('canvas');
  canvas.className = 'sprite-canvas';
  canvas.setAttribute('aria-label', `${entity.displayName} sprite animation`);
  canvasWrap.appendChild(canvas);
  section.appendChild(canvasWrap);

  const controls = buildAnimControls(canvas);
  section.appendChild(controls);

  // Load image then start rendering
  const img = new Image();
  img.onload = () => {
    detailState.image = img;
    // Derive frame layout from image dimensions.
    // Convention: sprite-atlas is a horizontal strip;
    // frame height == image height, frame width derived from animation count.
    const animCount = Array.isArray(entity.animations) && entity.animations.length > 0
      ? entity.animations.length
      : 1;
    detailState.frameCount = animCount;
    detailState.frameH = img.height;
    detailState.frameW = Math.floor(img.width / animCount);

    // Size canvas to a readable pixel-art display (3× scale, min 96px)
    const displaySize = Math.max(96, detailState.frameW * 3);
    canvas.width = detailState.frameW;
    canvas.height = detailState.frameH;
    canvas.style.width = `${displaySize}px`;
    canvas.style.height = `${Math.round(detailState.frameH * (displaySize / detailState.frameW))}px`;

    drawFrame(canvas);
    setPlaying(canvas, controls, true);
  };
  img.onerror = () => {
    canvasWrap.innerHTML = '';
    canvasWrap.appendChild(buildPlaceholder(entity.displayName));
    controls.remove();
  };
  img.src = `./assets/${entity.spriteAtlasFile}`;

  return section;
}

function buildAnimControls(canvas) {
  const controls = document.createElement('div');
  controls.className = 'anim-controls';

  const prevBtn = document.createElement('button');
  prevBtn.className = 'anim-btn';
  prevBtn.setAttribute('aria-label', 'Previous frame');
  prevBtn.innerHTML = `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>`;

  const playBtn = document.createElement('button');
  playBtn.className = 'anim-btn anim-btn-play';
  playBtn.setAttribute('aria-label', 'Play animation');
  playBtn.innerHTML = iconPlay();

  const nextBtn = document.createElement('button');
  nextBtn.className = 'anim-btn';
  nextBtn.setAttribute('aria-label', 'Next frame');
  nextBtn.innerHTML = `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>`;

  const frameLabel = document.createElement('span');
  frameLabel.className = 'anim-frame-label';
  updateFrameLabel(frameLabel);

  prevBtn.addEventListener('click', () => {
    setPlaying(canvas, controls, false);
    detailState.frameIndex =
      (detailState.frameIndex - 1 + detailState.frameCount) % Math.max(detailState.frameCount, 1);
    drawFrame(canvas);
    updateFrameLabel(frameLabel);
  });

  nextBtn.addEventListener('click', () => {
    setPlaying(canvas, controls, false);
    detailState.frameIndex =
      (detailState.frameIndex + 1) % Math.max(detailState.frameCount, 1);
    drawFrame(canvas);
    updateFrameLabel(frameLabel);
  });

  playBtn.addEventListener('click', () => {
    setPlaying(canvas, controls, !detailState.isPlaying);
  });

  controls.appendChild(prevBtn);
  controls.appendChild(playBtn);
  controls.appendChild(nextBtn);
  controls.appendChild(frameLabel);

  return controls;
}

function buildAnimList(animations) {
  const section = document.createElement('div');
  section.className = 'detail-section';

  const label = document.createElement('p');
  label.className = 'detail-section-label';
  label.textContent = 'Animations';
  section.appendChild(label);

  const list = document.createElement('div');
  list.className = 'detail-anim-list';
  for (const anim of animations) {
    const chip = document.createElement('span');
    chip.className = 'anim-chip';
    chip.textContent = anim;
    list.appendChild(chip);
  }
  section.appendChild(list);
  return section;
}

function buildPaletteSection(colorPalette) {
  const section = document.createElement('div');
  section.className = 'detail-section';

  const label = document.createElement('p');
  label.className = 'detail-section-label';
  label.textContent = 'Color Palette';
  section.appendChild(label);

  const grid = document.createElement('div');
  grid.className = 'detail-palette-grid';
  grid.setAttribute('aria-label', 'Color palette');

  for (const color of colorPalette) {
    const item = document.createElement('div');
    item.className = 'detail-palette-item';

    const swatch = document.createElement('div');
    swatch.className = 'detail-swatch';
    swatch.style.backgroundColor = color.hex;
    swatch.title = color.hex;

    const name = document.createElement('span');
    name.className = 'detail-swatch-name';
    name.textContent = color.name;

    const hex = document.createElement('span');
    hex.className = 'detail-swatch-hex';
    hex.textContent = color.hex;

    item.appendChild(swatch);
    item.appendChild(name);
    item.appendChild(hex);
    grid.appendChild(item);
  }

  section.appendChild(grid);
  return section;
}

function buildJsonSection(entity) {
  const section = document.createElement('details');
  section.className = 'detail-spec';
  section.open = false;

  const summary = document.createElement('summary');
  summary.textContent = 'JSON Specification';
  section.appendChild(summary);

  const pre = document.createElement('pre');
  pre.className = 'detail-json';
  pre.textContent = JSON.stringify(entity, null, 2);
  section.appendChild(pre);

  return section;
}

// ── Canvas animation ──────────────────────────────────────────────────────────

/**
 * Draws the current frame from the sprite atlas onto the canvas.
 */
function drawFrame(canvas) {
  if (!detailState.image || detailState.frameW === 0) return;
  const ctx = canvas.getContext('2d');
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  ctx.imageSmoothingEnabled = false;
  ctx.drawImage(
    detailState.image,
    detailState.frameIndex * detailState.frameW, 0,
    detailState.frameW, detailState.frameH,
    0, 0,
    canvas.width, canvas.height
  );
}

/**
 * Starts or stops the rAF animation loop.
 */
function setPlaying(canvas, controls, shouldPlay) {
  const playBtn = controls.querySelector('.anim-btn-play');

  if (shouldPlay && !detailState.isPlaying) {
    detailState.isPlaying = true;
    detailState.lastFrameTime = performance.now();
    if (playBtn) {
      playBtn.innerHTML = iconPause();
      playBtn.setAttribute('aria-label', 'Pause animation');
    }
    animLoop(canvas, controls);
  } else if (!shouldPlay && detailState.isPlaying) {
    stopAnimation();
    if (playBtn) {
      playBtn.innerHTML = iconPlay();
      playBtn.setAttribute('aria-label', 'Play animation');
    }
  }
}

function animLoop(canvas, controls) {
  if (!detailState.isPlaying) return;
  detailState.rafId = requestAnimationFrame((now) => {
    const interval = 1000 / detailState.fps;
    if (now - detailState.lastFrameTime >= interval) {
      detailState.frameIndex =
        (detailState.frameIndex + 1) % Math.max(detailState.frameCount, 1);
      drawFrame(canvas);
      const frameLabel = controls.querySelector('.anim-frame-label');
      if (frameLabel) updateFrameLabel(frameLabel);
      detailState.lastFrameTime = now;
    }
    animLoop(canvas, controls);
  });
}

function updateFrameLabel(el) {
  el.textContent = detailState.frameCount > 0
    ? `${detailState.frameIndex + 1} / ${detailState.frameCount}`
    : '';
}

// ── Helpers ───────────────────────────────────────────────────────────────────

function buildPlaceholder(displayName) {
  const el = document.createElement('div');
  el.className = 'detail-sprite-placeholder';
  el.setAttribute('role', 'img');
  el.setAttribute('aria-label', `${displayName} — no sprite`);
  el.textContent = displayName ? displayName.charAt(0).toUpperCase() : '?';
  return el;
}

function iconPlay() {
  return `<svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" stroke="none"><polygon points="5 3 19 12 5 21 5 3"/></svg>`;
}

function iconPause() {
  return `<svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" stroke="none"><rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/></svg>`;
}
