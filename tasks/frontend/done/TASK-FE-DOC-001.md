# TASK-FE-DOC-001: Создать структуру директории `docs-site/` и `main.js`

| Поле | Значение |
|------|----------|
| **ID** | TASK-FE-DOC-001 |
| **Тип** | frontend |
| **Компонент** | `docs-site/` |
| **Исполнитель** | JavaScript Developer |
| **Приоритет** | Высокий |
| **Статус** | ✅ done |
| **Зависимости** | TASK-BE-DOC-001 |
| **Связанные спецификации** | [`docs/specs/technical/visual-docs-site-structure.md`](../../../docs/specs/technical/visual-docs-site-structure.md) |
| **ADR** | [ADR-001](../../../docs/decisions/ADR-001-visual-docs-data-independence.md) |
| **Коммит** | f98c433e5507b5e0faf4d9d1e69efe4549d549f5 |

---

## Реализовано

- [x] `docs-site/index.html` — пустые контейнеры, filter bar, detail panel, theme toggle.
- [x] `docs-site/js/main.js` — `fetch('./data/docs-preview.json')` → filter → renderer.
- [x] `docs-site/js/renderer.js` — `createCard(entity)` только по полям объекта.
- [x] `docs-site/js/filter.js` — `filterByType` / `filterByName`.
- [x] `docs-site/js/detail.js` — панель деталей с JSON-спец.
- [x] `docs-site/css/style.css` — Nexus Design System, card grid, responsive.
- [x] `docs-site/data/.gitkeep`.

## Критерии приёмки

- [x] Сайт отображает N карточек при наличии `docs-preview.json`.
- [x] `grep -rn 'tanya\|sam\|aijan' docs-site/js/` → 0 результатов.
- [x] Фильтр по `type` работает (characters/locations/furniture/ui).
- [x] `spriteAtlasFile === null` — placeholder без ошибок в console.
- [x] Сайт открывается через `npx serve docs-site` без 404.
