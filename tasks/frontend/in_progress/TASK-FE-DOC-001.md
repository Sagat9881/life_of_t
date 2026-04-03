# TASK-FE-DOC-001: Создать структуру директории `docs-site/` и `main.js`

| Поле | Значение |
|------|----------|
| **ID** | TASK-FE-DOC-001 |
| **Тип** | frontend |
| **Компонент** | `docs-site/` (новая директория) |
| **Исполнитель** | JavaScript Developer |
| **Приоритет** | Высокий |
| **Зависимости** | TASK-BE-DOC-001 (нужен `docs-preview.json` для тестирования) |
| **Связанные спецификации** | [`docs/specs/technical/visual-docs-site-structure.md`](../../../docs/specs/technical/visual-docs-site-structure.md) |
| **ADR** | [ADR-001](../../../docs/decisions/ADR-001-visual-docs-data-independence.md) |
| **Статус** | 🔄 in_progress |

---

## Описание

Создать файловую структуру `docs-site/` согласно техспеку и реализовать data-driven
загрузку данных в `main.js`.

## Задачи реализации

1. Создать `docs-site/index.html` с пустыми контейнерами.
2. Создать `docs-site/js/main.js`: `fetch('./data/docs-preview.json')` → итерация → вызов `renderer.js`.
3. Создать `docs-site/js/renderer.js`: `createCard(entity)` без хардкода имён.
4. Создать `docs-site/js/filter.js`: фильтрация по полю `type`.
5. Создать `docs-site/css/style.css`: базовая сетка карточек.
6. Добавить `docs-site/data/.gitkeep` (JSON придёт от CI).

## Критерии приёмки

- [ ] При наличии `docs-site/data/docs-preview.json` сайт отображает N карточек (N из JSON).
- [ ] `grep -rn 'tanya\|sam\|aijan' docs-site/js/` возвращает 0 результатов.
- [ ] Фильтр по `type` работает (characters/locations/furniture/ui).
- [ ] Отсутствие спрайта (`spriteAtlasFile === null`) не вызывает ошибок в console.
- [ ] Сайт открывается через `npx serve docs-site` без 404.
