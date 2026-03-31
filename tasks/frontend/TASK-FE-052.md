# TASK-FE-052 — Реализация Assets Gallery HTML+CSS+JS

**Тип:** frontend  
**Приоритет:** HIGH  
**Статус:** TODO  
**Роль:** JavaScript Developer  
**Связанная спецификация:** `docs/specs/STATUS_PAGE_ASSETS_SPEC.md` (разделы 3–6, 7)  
**Зависимость:** TASK-BE-021 (файл `assets-manifest.json` должен быть доступен)

## Описание

Реализовать раздел Assets Gallery в рамках Project Status Dashboard согласно спецификации SA-012.

## Что реализовать

1. **HTML-разметка:** `<section id="assets">` с `<nav class="assets-tabs">` и пятью `<div class="assets-panel" data-category="...">` (wireframe в разделе 3 спецификации).
2. **JS-функция `renderAssetsGallery(manifest)`** — принимает распарсенный манифест, рендерит все панели.
3. **JS-функция `switchCategory(categoryId)`** — переключает активный таб + обновляет URL hash.
4. **Загрузка манифеста:** `fetch('assets-manifest.json')` с обработкой ошибки (раздел 7.4).
5. **Навигация по hash** при загрузке страницы (раздел 5.2, п.3).
6. **Placeholder** для пустых категорий (раздел 7.1).

## Критерии приёмки

- [ ] Все 5 табов отрисовываются; счётчики корректны
- [ ] Переключение табов без перезагрузки страницы
- [ ] URL hash обновляется при смене таба
- [ ] Deep-link `#assets-{category}` активирует нужный таб
- [ ] Пустая категория показывает placeholder из спецификации (раздел 7.1)
- [ ] Нет JS-ошибок в консоли при любом состоянии манифеста
- [ ] `loading="lazy"` на всех `<img>`
- [ ] ARIA-атрибуты: `role="tablist"`, `role="tab"`, `aria-selected`, `aria-controls`
