/**
 * main.js — net + orchestration layer
 * Fetches docs-preview.json, wires up filter/search, delegates rendering.
 * ADR-001: no hardcoded entity IDs or names.
 */
import { createCard } from './renderer.js';
import { filterByType, filterByName } from './filter.js';
import { openDetail, initDetailPanel } from './detail.js';

const DATA_URL = './data/docs-preview.json';

const elGrid = document.getElementById('cards-grid');
const elLoading = document.getElementById('loading');
const elError = document.getElementById('error-state');
const elEmpty = document.getElementById('empty-state');
const elSearch = document.getElementById('search-input');
const filterButtons = document.querySelectorAll('.filter-btn');

let allEntities = [];
let activeType = 'all';
let activeQuery = '';

// ── Bootstrap ────────────────────────────────────────────────────────────────

async function init() {
  initDetailPanel();
  initThemeToggle();

  try {
    const response = await fetch(DATA_URL);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    allEntities = await response.json();
  } catch (err) {
    showError();
    console.error('[docs-site] Failed to load data:', err);
    return;
  } finally {
    elLoading.hidden = true;
  }

  renderAll();
  bindEvents();
}

// ── Render ────────────────────────────────────────────────────────────────────

function renderAll() {
  let entities = filterByType(allEntities, activeType);
  entities = filterByName(entities, activeQuery);

  elGrid.innerHTML = '';
  elEmpty.hidden = entities.length > 0;

  for (const entity of entities) {
    const card = createCard(entity);
    card.addEventListener('click', () => openDetail(entity));
    card.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        openDetail(entity);
      }
    });
    elGrid.appendChild(card);
  }
}

// ── Events ────────────────────────────────────────────────────────────────────

function bindEvents() {
  filterButtons.forEach((btn) => {
    btn.addEventListener('click', () => {
      activeType = btn.dataset.type;
      filterButtons.forEach((b) => {
        b.classList.toggle('active', b === btn);
        b.setAttribute('aria-pressed', String(b === btn));
      });
      renderAll();
    });
  });

  let searchDebounce;
  elSearch.addEventListener('input', () => {
    clearTimeout(searchDebounce);
    searchDebounce = setTimeout(() => {
      activeQuery = elSearch.value.trim();
      renderAll();
    }, 200);
  });
}

// ── Helpers ───────────────────────────────────────────────────────────────────

function showError() {
  elLoading.hidden = true;
  elError.hidden = false;
}

function initThemeToggle() {
  const toggle = document.querySelector('[data-theme-toggle]');
  const root = document.documentElement;
  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
  let currentTheme = prefersDark ? 'dark' : 'light';
  root.setAttribute('data-theme', currentTheme);

  if (!toggle) return;
  toggle.addEventListener('click', () => {
    currentTheme = currentTheme === 'dark' ? 'light' : 'dark';
    root.setAttribute('data-theme', currentTheme);
    toggle.setAttribute('aria-label', `Switch to ${currentTheme === 'dark' ? 'light' : 'dark'} mode`);
  });
}

init();
