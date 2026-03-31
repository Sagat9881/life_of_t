# TASK-FE-050 — Реализация Overview HTML+CSS+JS по спецификации SA-011

| Поле | Значение |
|------|----------|
| **ID** | TASK-FE-050 |
| **Тип** | frontend |
| **Статус** | TODO |
| **Приоритет** | HIGH |
| **Роль** | JS Developer |
| **Эпик** | EPIC-DEVOPS-001 |
| **Связанная спека** | `docs/specs/STATUS_PAGE_OVERVIEW_SPEC.md` |
| **Зависит от** | TASK-SA-011 |

## Описание

Реализовать статичную HTML-страницу `docs/status/index.html` с разделом Overview согласно `STATUS_PAGE_OVERVIEW_SPEC.md`. Страница использует только vanilla HTML/CSS/JS (ES2020), без фреймворков.

## Требования

1. Создать файлы:
   - `docs/status/index.html` — структура по wireframe из Раздела 1 спеки
   - `docs/status/status.css` — стили по Разделу 6 (pixel-art токены)
   - `docs/status/status.js` — JavaScript API по Разделу 5
2. Реализовать все функции из Раздела 5 спеки:
   - `init()`, `renderOverview()`, `createCard()`, `showDrilldown()`, `hideDrilldown()`, `renderDrilldownContent()`
   - вспомогательные: `formatRelativeTime()`, `createProgressBar()`, `createStatusBadge()`, `createBarChart()`, `computeStatus()`
3. Все 5 карточек рендерятся из `CARD_CONFIG` (см. §2.4 спеки)
4. Маппинг полей `metrics.json` → компоненты согласно Разделу 4 спеки
5. Цветовые пороги из `PROJECT_METRICS_SPEC.md` Приложение A

## Критерии приёмки

- [ ] `index.html` открывается локально (`file://`) без ошибок в консоли
- [ ] При отсутствии `metrics.json` — показывает баннер ошибки, не падает
- [ ] Все 5 карточек рендерятся с корректными значениями
- [ ] Прогресс-бары отображают правильные цвета по порогам
- [ ] Нет импортов фреймворков
- [ ] `aria-expanded` обновляется при открытии/закрытии drilldown
- [ ] Pixel-art стиль: `border-radius: 0`, пиксельные рамки
- [ ] Шрифт деградирует до `Courier New` если `Press Start 2P` недоступен

## Метрики, затрагиваемые задачей

- Все 5 групп (TBH, BC, AC, NC, CCH) — визуализация верхнеуровневых значений
