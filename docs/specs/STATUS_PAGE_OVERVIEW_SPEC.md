# STATUS_PAGE_OVERVIEW_SPEC.md

> **Версия:** 1.0  
> **Автор:** System Analyst  
> **Дата:** 2026-03-31  
> **Эпик:** EPIC-DEVOPS-001 — Project Status Dashboard  
> **Фаза:** 2 — HTML Overview страница  
> **Зависимость:** TASK-SA-010 ✅ (JSON-схема `metrics.json` задокументирована)  
> **Связанные задачи:** TASK-SA-011, TASK-FE-050, TASK-FE-051  
> **Ссылка на skill:** `system-analyst-skill.md` §6 «Технические спецификации», §8 «Система метрик»

---

## Раздел 1. Wireframe-схема

### 1.1 Общий layout страницы

```
┌────────────────────────────────────────────────────────────────┐
│  <header>                                                      │
│  ╔══════════════════════════════════════════════════════════╗  │
│  ║  🎮 Life of T — Project Status                          ║  │
│  ║  Generated: 2026-03-31T15:39:00Z   [→ Assets Gallery]  ║  │
│  ╚══════════════════════════════════════════════════════════╝  │
├────────────────────────────────────────────────────────────────┤
│  <nav>                                                         │
│  [ Overview ✦ ]  [ Assets Gallery ]                           │
├────────────────────────────────────────────────────────────────┤
│  <main id="overview">                                          │
│                                                                │
│  <section class="metrics-grid">                               │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────┐ │
│  │ 📋       │ │ ☕       │ │ 🎨       │ │ 📖       │ │ ⚙️  │ │
│  │ Task     │ │ Backend  │ │ Assets   │ │Narrative │ │CI/CD │ │
│  │ Board    │ │Completns.│ │Coverage  │ │Coverage  │ │Health│ │
│  │          │ │          │ │          │ │          │ │      │ │
│  │  42%     │ │   3/8    │ │  100%    │ │   12     │ │  🟢  │ │
│  │ ████░░░  │ │ ███░░░░  │ │ ████████ │ │  units   │ │  OK  │ │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────┘ │
│                                                                │
│  <div class="drilldown-panel" hidden>                         │
│  ┌────────────────────────────────────────────────────────┐   │
│  │ ▼ Task Board Health                             [✕]    │   │
│  │                                                        │   │
│  │  Total tasks         42    ████████████████████ 100%  │   │
│  │  Done                18    ████████░░░░░░░░░░░░  43%  │   │
│  │  In Progress          6    ███░░░░░░░░░░░░░░░░░  14%  │   │
│  │  Blocked              3    ██░░░░░░░░░░░░░░░░░░   7%  │   │
│  │  By role:  [BE 60%] [FE 30%] [AS 80%] [NR 20%]       │   │
│  └────────────────────────────────────────────────────────┘   │
│                                                                │
└────────────────────────────────────────────────────────────────┤
│  <footer>                                                      │
│  v1.0 · SHA: c95f41c · Life of T © 2026                       │
└────────────────────────────────────────────────────────────────┘
```

### 1.2 Состояния страницы

| Состояние | Описание |
|-----------|----------|
| `loading` | Отображается skeleton-заглушка пока грузится `metrics.json` |
| `loaded` | Все 5 карточек отрисованы с данными |
| `error` | Баннер ошибки если `metrics.json` недоступен |
| `drilldown-open` | Одна карточка раскрыта, drilldown-панель видна |

### 1.3 Responsive breakpoints

| Ширина | Layout метрик-сетки |
|--------|---------------------|
| ≥ 1024px | 5 колонок (все карточки в ряд) |
| 640–1023px | 3 + 2 колонки (2 ряда) |
| < 640px | 1 колонка (стек) |

---

## Раздел 2. Компонент `.metric-card`

### 2.1 HTML-структура

```html
<article
  class="metric-card"
  data-category="taskBoard"          <!-- ID категории: taskBoard|backend|assets|narrative|cicd -->
  data-status="warning"              <!-- ok | warning | critical | pending -->
  aria-expanded="false"
  role="button"
  tabindex="0"
>
  <div class="metric-card__icon" aria-hidden="true">📋</div>

  <div class="metric-card__body">
    <h2 class="metric-card__title">Task Board</h2>
    <div class="metric-card__value">42%</div>
    <div class="metric-card__visual">
      <!-- для процентов: -->
      <div class="progress-bar" role="progressbar"
           aria-valuenow="42" aria-valuemin="0" aria-valuemax="100">
        <div class="progress-bar__fill" style="width: 42%"></div>
      </div>
      <!-- для статуса: -->
      <!-- <span class="status-badge status-badge--success">✓ success</span> -->
      <!-- для счётчика без прогресса — значение уже в .metric-card__value -->
    </div>
  </div>

  <button class="metric-card__expand" aria-label="Show details" tabindex="-1">▼</button>
</article>
```

### 2.2 CSS-классы карточки

| Класс | Назначение |
|-------|------------|
| `.metric-card` | Контейнер карточки; cursor: pointer |
| `.metric-card--active` | Карточка, drilldown которой открыт |
| `.metric-card[data-status="ok"]` | Зелёная рамка (`--color-ok`) |
| `.metric-card[data-status="warning"]` | Жёлтая рамка (`--color-warning`) |
| `.metric-card[data-status="critical"]` | Красная рамка (`--color-critical`) |
| `.metric-card[data-status="pending"]` | Серая рамка (`--color-pending`) |
| `.metric-card__icon` | Иконка 32×32px, pixel-art emoji |
| `.metric-card__title` | Подпись категории, `font-size: var(--font-sm)` |
| `.metric-card__value` | Главное число/текст, `font-size: var(--font-xl)` |
| `.metric-card__visual` | Зона прогресс-бара или бейджа |
| `.metric-card__expand` | Стрелка-индикатор открытия, поворачивается при `.--active` |

### 2.3 JS-поведение карточки

- При клике или `Enter`/`Space` — вызывает `showDrilldown(categoryId)` или `hideDrilldown()` (toggle)
- Добавляет/снимает класс `.metric-card--active`
- Устанавливает `aria-expanded="true" / "false"`
- Только одна карточка может быть активна одновременно (предыдущая закрывается)

### 2.4 Конфигурация карточек

| `data-category` | Иконка | Название | Главное значение (поле `metrics.json`) | Визуализация |
|-----------------|--------|----------|----------------------------------------|--------------|
| `taskBoard` | 📋 | Task Board | `taskBoard.completionPct` | progress-bar |
| `backend` | ☕ | Backend | `backend.aggregates` + `/` + `TARGET` | counter |
| `assets` | 🎨 | Assets | `assets.metadataCoveragePct` | progress-bar |
| `narrative` | 📖 | Narrative | `narrative.total` | counter |
| `cicd` | ⚙️ | CI/CD | `cicd.lastRunStatus` | status-badge |

---

## Раздел 3. Компонент `.drilldown-panel`

### 3.1 HTML-структура

```html
<section
  class="drilldown-panel"
  id="drilldown"
  aria-live="polite"
  hidden
>
  <div class="drilldown-panel__header">
    <span class="drilldown-panel__title">Task Board Health</span>
    <button class="drilldown-panel__close" aria-label="Close details">✕</button>
  </div>

  <ul class="drilldown-panel__list">
    <!-- генерируется JS для каждой sub-метрики -->
    <li class="drilldown-item">
      <span class="drilldown-item__label">Done</span>
      <span class="drilldown-item__value">18</span>
      <div class="progress-bar">
        <div class="progress-bar__fill" style="width: 43%"></div>
      </div>
    </li>
    <!-- ... повторяется для каждой sub-метрики -->
  </ul>

  <!-- для категориального распределения (byRole, byCategory) -->
  <div class="drilldown-panel__chart" aria-label="Distribution chart">
    <!-- .bar-chart — CSS-only столбчатая диаграмма -->
    <div class="bar-chart">
      <div class="bar-chart__bar" style="height: 60%" data-label="BE" data-value="60%"></div>
      <div class="bar-chart__bar" style="height: 30%" data-label="FE" data-value="30%"></div>
      <div class="bar-chart__bar" style="height: 80%" data-label="AS" data-value="80%"></div>
      <div class="bar-chart__bar" style="height: 20%" data-label="NR" data-value="20%"></div>
    </div>
  </div>
</section>
```

### 3.2 Анимация

| Состояние | CSS-переход |
|-----------|-------------|
| Открытие | `max-height: 0 → max-height: 600px`, `opacity: 0 → 1`, duration `200ms ease-out` |
| Закрытие | обратный переход, duration `150ms ease-in` |
| Смена категории | fade-out (100ms) → смена содержимого → fade-in (100ms) |

Панель вставляется в DOM сразу после `.metrics-grid`, чтобы занимать полную ширину сетки (grid-column: 1 / -1).

### 3.3 Позиция в grid

```css
/* Панель всегда занимает всю ширину сетки */
.drilldown-panel {
  grid-column: 1 / -1;
  /* вставляется JS между .metrics-grid и footer */
}
```

### 3.4 Sub-метрики по категориям

#### taskBoard
| Поле JSON | Метка | Визуализация |
|-----------|-------|--------------|
| `taskBoard.total` | Total tasks | число |
| `taskBoard.done` | Done | число + progress-bar (`done/total*100`) |
| `taskBoard.inProgress` | In Progress | число |
| `taskBoard.blocked` | Blocked | число + status-badge (🔴 если > 0) |
| `taskBoard.readyToStart` | Ready to start | число |
| `taskBoard.byRole` | By role | bar-chart (BE/FE/AS/NR %) |

#### backend
| Поле JSON | Метка | Визуализация |
|-----------|-------|--------------|
| `backend.aggregates` | Aggregates | число |
| `backend.services` | Services | число |
| `backend.controllers` | Controllers | число |
| `backend.testCoveragePct` | Test coverage | progress-bar |
| `backend.narrativeSpecsWithoutHardcode` | Specs w/o hardcode | число |

#### assets
| Поле JSON | Метка | Визуализация |
|-----------|-------|--------------|
| `assets.totalAtlases` | Total atlases | число |
| `assets.atlasesWithMetadata` | With metadata | число + progress-bar (`metadataCoveragePct`) |
| `assets.byCategory.npc` | NPC | число |
| `assets.byCategory.locations` | Locations | число |
| `assets.locationsComplete` | Locations complete | число |
| `assets.npcComplete` | NPC complete | число |

#### narrative
| Поле JSON | Метка | Визуализация |
|-----------|-------|--------------|
| `narrative.quests` | Quests | число |
| `narrative.conflicts` | Conflicts | число |
| `narrative.npc` | NPC blocks | число |
| `narrative.worldEvents` | World events | число |
| `narrative.withManifest` | With manifest | число + progress-bar (`manifestCoveragePct`) |

#### cicd
| Поле JSON | Метка | Визуализация |
|-----------|-------|--------------|
| `cicd.lastRunStatus` | Last run | status-badge |
| `cicd.lastRunAt` | Run time | relative-time |
| `cicd.lastSuccessAt` | Last success | relative-time |
| `cicd.dockerfileExists` | Dockerfile | ✅/❌ badge |
| `cicd.dockerComposeExists` | docker-compose.yml | ✅/❌ badge |
| `cicd.pagesStatus` | GitHub Pages | status-badge + ссылка |

---

## Раздел 4. Маппинг `metrics.json` → компоненты

```
metrics.json
│
├── generatedAt ──────────────────────► <header> .header__generated-at
│                                        (ISO → relative time)
│
├── taskBoard
│   ├── completionPct ────────────────► .metric-card[taskBoard] .metric-card__value
│   │                                   + .progress-bar__fill width
│   ├── done / total / inProgress ────► .drilldown-panel (taskBoard) list items
│   ├── blocked ──────────────────────► .drilldown-panel + status-badge
│   ├── readyToStart ─────────────────► .drilldown-panel list item
│   └── byRole ───────────────────────► .drilldown-panel .bar-chart
│
├── backend
│   ├── aggregates ───────────────────► .metric-card[backend] .metric-card__value
│   │                                   (формат: "N / TARGET")
│   ├── services / controllers ───────► .drilldown-panel list items
│   ├── testCoveragePct ──────────────► .drilldown-panel progress-bar
│   └── narrativeSpecsWithoutHardcode ► .drilldown-panel list item
│
├── assets
│   ├── metadataCoveragePct ──────────► .metric-card[assets] .metric-card__value
│   │                                   + .progress-bar__fill width
│   ├── totalAtlases / atlasesWithMetadata ► .drilldown-panel list items
│   ├── byCategory ───────────────────► .drilldown-panel (нет bar-chart, числа)
│   └── locationsComplete / npcComplete ► .drilldown-panel list items
│
├── narrative
│   ├── total ────────────────────────► .metric-card[narrative] .metric-card__value
│   ├── quests / conflicts / npc / worldEvents ► .drilldown-panel list items
│   └── withManifest + manifestCoveragePct ► .drilldown-panel list item + progress-bar
│
└── cicd
    ├── lastRunStatus ────────────────► .metric-card[cicd] .status-badge (цвет)
    ├── lastRunAt ────────────────────► .metric-card[cicd] подпись (relative time)
    ├── lastSuccessAt ────────────────► .drilldown-panel list item
    ├── dockerfileExists ─────────────► .drilldown-panel ✅/❌
    ├── dockerComposeExists ──────────► .drilldown-panel ✅/❌
    └── pagesUrl ─────────────────────► <header> ссылка «→ Live page»
```

### 4.1 Вычисление `data-status` карточки

| Категория | `ok` | `warning` | `critical` |
|-----------|------|-----------|------------|
| taskBoard | `completionPct ≥ 67` | `34–66` | `< 34` |
| backend | `aggregates ≥ 5` | `aggregates 2–4` | `aggregates < 2` |
| assets | `metadataCoveragePct = 100` | `50–99` | `< 50` |
| narrative | `total ≥ 10` | `total 4–9` | `total < 4` |
| cicd | `lastRunStatus = success` | `pending / cancelled` | `failure` |

---

## Раздел 5. JavaScript API

Весь JavaScript — в одном файле `status.js`. Нет зависимостей, только vanilla ES2020.

### 5.1 Точка входа

```js
/**
 * Загружает metrics.json и инициализирует страницу.
 * Вызывается при DOMContentLoaded.
 */
async function init(): Promise<void>
```

Поведение:
1. `fetch('./metrics.json')` с обработкой ошибки (показать `#error-banner`)
2. Вызов `renderOverview(metrics)`
3. Вызов `renderHeader(metrics.generatedAt)`

---

### 5.2 `renderOverview(metrics)`

```js
/**
 * Рендерит все 5 .metric-card в .metrics-grid.
 * @param {Object} metrics — объект из metrics.json
 */
function renderOverview(metrics: MetricsJson): void
```

Поведение:
- Итерирует массив `CARD_CONFIG` (см. §2.4) — 5 элементов
- Для каждого вызывает `createCard(category, metrics)` → вставляет в `.metrics-grid`
- Навешивает обработчик клика: `card.addEventListener('click', () => toggleDrilldown(categoryId))`

---

### 5.3 `createCard(categoryId, metrics)`

```js
/**
 * Создаёт DOM-элемент .metric-card для одной категории.
 * @param {string} categoryId — 'taskBoard' | 'backend' | 'assets' | 'narrative' | 'cicd'
 * @param {Object} metrics — объект из metrics.json
 * @returns {HTMLElement}
 */
function createCard(categoryId: string, metrics: MetricsJson): HTMLElement
```

Поведение:
- Берёт конфиг из `CARD_CONFIG[categoryId]`
- Вычисляет `data-status` по таблице из §4.1
- Форматирует главное значение (`formatValue(raw, type)`)
- Возвращает готовый `<article class="metric-card">`

---

### 5.4 `showDrilldown(categoryId)`

```js
/**
 * Открывает .drilldown-panel с sub-метриками выбранной категории.
 * Закрывает предыдущую если открыта другая.
 * @param {string} categoryId
 */
function showDrilldown(categoryId: string): void
```

Поведение:
1. Если `currentDrilldown === categoryId` → вызвать `hideDrilldown()`
2. Иначе: вызвать `hideDrilldown()`, дождаться 100ms (fade-out)
3. Заполнить `.drilldown-panel` через `renderDrilldownContent(categoryId, metrics)`
4. Снять `hidden`, добавить класс `.drilldown-panel--visible`
5. Обновить активную карточку: `.metric-card--active`
6. Установить `currentDrilldown = categoryId`

---

### 5.5 `hideDrilldown()`

```js
/**
 * Скрывает .drilldown-panel.
 * Снимает .metric-card--active со всех карточек.
 */
function hideDrilldown(): void
```

---

### 5.6 `renderDrilldownContent(categoryId, metrics)`

```js
/**
 * Заполняет внутренности .drilldown-panel для категории.
 * @param {string} categoryId
 * @param {Object} metrics
 */
function renderDrilldownContent(categoryId: string, metrics: MetricsJson): void
```

Поведение:
- По `categoryId` берёт данные из соответствующего раздела `metrics`
- Генерирует `<li class="drilldown-item">` для каждой sub-метрики из таблиц §3.4
- Для типа `percent` → вызывает `createProgressBar(value)`
- Для типа `status` → вызывает `createStatusBadge(value)`
- Для типа `datetime` → вызывает `formatRelativeTime(isoString)`
- Для типа `distribution` → вызывает `createBarChart(data)`

---

### 5.7 Вспомогательные функции

```js
/** Форматирует ISO-8601 в «2 hours ago» / «3 days ago» */
function formatRelativeTime(isoString: string): string

/** Создаёт <div class="progress-bar"> с fill по % */
function createProgressBar(pct: number): HTMLElement

/** Создаёт <span class="status-badge"> по строке статуса */
function createStatusBadge(status: string): HTMLElement

/** Создаёт CSS-only .bar-chart из объекта {label: value%} */
function createBarChart(data: Record<string, number>): HTMLElement

/** Вычисляет data-status ('ok'|'warning'|'critical'|'pending') */
function computeStatus(categoryId: string, metrics: MetricsJson): string
```

---

## Раздел 6. CSS-переменные и pixel-art токены

### 6.1 Цветовые токены

```css
:root {
  /* Статусные цвета */
  --color-ok:       #27ae60;  /* зелёный — 67–100% */
  --color-warning:  #f39c12;  /* жёлтый  — 34–66%  */
  --color-critical: #e74c3c;  /* красный — 0–33%   */
  --color-pending:  #7f8c8d;  /* серый   — ожидание */

  /* Фон страницы — тёмная пиксельная тема */
  --color-bg:          #1a1a2e;  /* тёмно-синий фон */
  --color-bg-card:     #16213e;  /* карточка */
  --color-bg-drilldown:#0f3460;  /* панель детализации */
  --color-border:      #533483;  /* пиксельная рамка */
  --color-text:        #e0e0e0;  /* основной текст */
  --color-text-muted:  #a0a0b0;  /* вторичный текст */
  --color-accent:      #e94560;  /* акцент (Life of T pink) */

  /* Прогресс-бар */
  --color-bar-bg:   #2a2a4a;
  --color-bar-fill: var(--color-ok);  /* переопределяется inline */
}
```

### 6.2 Типографика (pixel-art шрифт)

```css
:root {
  /* Pixel-art шрифт: приоритет системным моноширинным */
  --font-family: 'Press Start 2P', 'Courier New', monospace;

  --font-xs: 8px;   /* мелкие подписи */
  --font-sm: 10px;  /* названия карточек */
  --font-md: 12px;  /* значения sub-метрик */
  --font-lg: 16px;  /* заголовок панели */
  --font-xl: 24px;  /* главное значение карточки */
}
```

> **Примечание для JS Developer:** шрифт «Press Start 2P» подключается через Google Fonts только если доступен. Реализация должна корректно деградировать до `Courier New`.

### 6.3 Отступы и размеры

```css
:root {
  --space-xs:  4px;
  --space-sm:  8px;
  --space-md: 16px;
  --space-lg: 24px;
  --space-xl: 32px;

  --card-min-width:  160px;
  --card-padding:    var(--space-md);
  --card-border-width: 2px;  /* pixel-art: чёткие рамки без blur */
  --card-border-radius: 0;   /* pixel-art: прямые углы */

  --progress-height: 8px;    /* высота прогресс-бара */
  --bar-chart-height: 64px;  /* высота столбиков диаграммы */
}
```

### 6.4 Pixel-art визуальные правила

1. `border-radius: 0` — никаких скруглений
2. `image-rendering: pixelated` для любых `<img>`
3. `box-shadow: 2px 2px 0 var(--color-border)` вместо `box-shadow` с blur
4. Анимации через `steps()` timing function для имитации pixel-движения (не обязательно для прогресс-баров, только для иконок если будут)
5. Прогресс-бар: высота 8px, без скруглений, цвет меняется по порогу через `data-status`

### 6.5 CSS-файловая структура

JS Developer реализует стили в одном файле `status.css`:

```
status.css
├── :root { CSS-переменные }
├── Reset / base styles
├── Layout: header, nav, main, footer
├── .metrics-grid
├── .metric-card + модификаторы
├── .progress-bar + .progress-bar__fill
├── .status-badge + модификаторы
├── .drilldown-panel + анимации
├── .drilldown-item
├── .bar-chart + .bar-chart__bar
└── Responsive breakpoints
```

---

## Приложение A. Файловая структура артефактов

```
docs/status/
├── index.html          ← точка входа (GitHub Pages)
├── status.css          ← все стили
├── status.js           ← весь JS (vanilla ES2020)
└── metrics.json        ← генерируется пайплайном (не коммитится вручную)
```

> `metrics.json` генерируется GitHub Actions при каждом пуше в `main` и не должен коммититься вручную.

---

## Приложение B. Критерии готовности реализации

- [ ] `index.html` открывается локально (`file://`) без ошибок в консоли
- [ ] При отсутствии `metrics.json` — показывает баннер ошибки, не падает
- [ ] Все 5 карточек рендерятся с корректными значениями из `metrics.json`
- [ ] Клик по карточке открывает drilldown, повторный клик закрывает
- [ ] Одновременно открыт только один drilldown
- [ ] Цвет прогресс-бара соответствует порогам из `PROJECT_METRICS_SPEC.md` Приложение A
- [ ] Страница читаема без JS (статический fallback)
- [ ] Нет импортов фреймворков (React, Vue, Alpine, etc.)
- [ ] `aria-expanded` обновляется корректно
- [ ] Pixel-art стиль: прямые углы, пиксельные рамки
