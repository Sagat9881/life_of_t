# Техспек: структура документационного сайта Life of T

| Поле | Значение |
|------|----------|
| **Путь** | `docs/specs/technical/visual-docs-site-structure.md` |
| **Компонент** | `docs-site/` (новая директория) |
| **SDD-фаза** | Specify |
| **Дата** | 2026-04-03 |
| **Ответственный** | System Analyst |
| **Исполнитель** | JavaScript Developer |
| **ADR** | [ADR-001](../decisions/ADR-001-visual-docs-data-independence.md) |
| **Задачи** | `tasks/frontend/TASK-FE-DOC-001.md`, `tasks/frontend/TASK-FE-DOC-002.md` |

---

## 1. Контекст и цель

Документационный сайт — **pure static** HTML/CSS/JS-приложение, собираемое GitHub Actions из двух источников данных:
1. `docs-preview.json` — дескрипторы сущностей (из генератора ассетов).
2. PNG-спрайты — из CI-артефакта `generated-pixel-art-assets`.

Сайт публикуется в GitHub Pages. Никакой серверной части нет.

**Ключевой принцип**: сайт не содержит ни одного имени персонажа, локации или компонента в своём JS/HTML/CSS коде. Все данные приходят из `docs-preview.json`.

---

## 2. Директория `docs-site/`

```
docs-site/
├── index.html          # точка входа, содержит пустые контейнеры
├── assets/             # PNG-спрайты (копируются CI из артефакта)
├── data/
│   └── docs-preview.json  # копируется CI из артефакта генератора
├── css/
│   └── style.css
└── js/
    ├── main.js         # точка входа: fetch data, init
    ├── renderer.js     # отрисовка карточек
    ├── filter.js       # фильтрация по типу (characters/locations/etc)
    └── detail.js       # панель деталей (анимация, спецификация)
```

---

## 3. Функциональные требования

### FR-1 — Data-driven инициализация

При загрузке `main.js`:
1. `fetch('./data/docs-preview.json')`
2. Для каждого объекта в массиве: создать карточку через `renderer.js`.
3. Никаких `if (entity.id === 'tanya')` в коде нет.

### FR-2 — Карточка сущности

Каждая карточка содержит:
- Спрайт-превью (PNG из `assets/{spriteAtlasFile}`, если не null).
- `displayName` как заголовок.
- `type` как категорию (badge).
- Список анимаций.
- Цветовая палитра (swatches).

### FR-3 — Интерактивность

- Клик по карточке → открытие панели деталей (`detail.js`):
  - Анимация спрайта (если PNG доступен): цикл кадров по sprite-atlas.
  - Раскрытие JSON-спецификации (pretty-print).
- Фильтр по `type` (characters / locations / furniture / ui) — без перезагрузки страницы.
- Поиск по `displayName`.

### FR-4 — Отсутствие спрайта

Если `spriteAtlasFile === null` — показывать placeholder-иконку, не ломать отрисовку.

---

## 4. Нефункциональные требования

- **NFR-1**: Сайт работает без JS-фреймворков (Vanilla JS). Допустима CSS-библиотека (Bootstrap, Tailwind CDN).
- **NFR-2**: Время первой отрисовки карточек ≤ 2 секунды на стандартном соединении.
- **NFR-3**: Сайт работает при открытии через `file://` (для локальной разработки без сервера) — или через `npx serve`.
- **NFR-4**: Никаких внешних API-запросов кроме загрузки CSS-библиотеки с CDN.

---

## 5. Контракт данных (API сайта)

Сайт потребляет `docs-preview.json`. Формат описан в
[`visual-docs-preview-mode.md`](./visual-docs-preview-mode.md), раздел FR-2.

JS-код должен обрабатывать `null` в полях `spriteAtlasFile` и `constraints` без падения.

---

## 6. Метрики и критерии готовности

| Критерий | Измерение |
|----------|-----------|
| Сайт загружается без ошибок в console | Ручная проверка / Playwright smoke |
| Количество карточек == количество объектов в `docs-preview.json` | Playwright assertion |
| Фильтр по type работает корректно | Playwright |
| Клик по карточке открывает панель деталей | Playwright |
| В JS/HTML/CSS нет имён персонажей | grep в CI |
