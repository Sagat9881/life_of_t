# TASK-FE-DOC-002: Реакции на клики — AnimationPreview и SpecPanel

**Тип:** frontend  
**Статус:** todo  
**Роль:** JavaScript Developer  
**Skill-файл:** `javascript-developer-skill.md`  
**Зависит от:** TASK-FE-DOC-001 (компонент EntityCard должен быть готов)

---

## Контекст

Карточки документационного сайта должны реагировать на клики согласно полю `click-action` из `card-meta.json` (значения: `show-animation`, `open-spec`, `both`). Поведение полностью определяется данными, а не кодом.

---

## Связанные спецификации

- `docs/specs/technical/visual-docs-site-structure.md` — компоненты `AnimationPreview`, `SpecPanel`.
- `docs/specs/technical/visual-docs-preview-mode.md` — формат `card-meta.json`, поле `click-action`.
- `javascript-developer-skill.md` (разделы 3, 4) — обработка событий, запрет хардкода.
- `docs/decisions/ADR-001-visual-docs-data-independence.md`.

---

## Что нужно сделать

1. **AnimationPreview**: при клике на карточку с `click-action: show-animation` или `both` — загрузить PNG по пути `previewPath` из `card-meta.json` и отобразить. Загрузка — lazy (только при клике, не при старте страницы).
2. **SpecPanel**: при `click-action: open-spec` или `both` — загрузить markdown-файл из `specPath`, конвертировать в HTML (минимальный `marked.js` или совместимый аналог), показать в раскрываемой панели.
3. Обработчик кликов — **универсальный**, привязан к `entityId` из данных. Запрещено: `if (entityId === 'tanya') { ... }` или аналогичное.
4. При `click-action: both` — показывать оба компонента. **Решение об UI**: показывать параллельно (превью слева, спека справа) — зафиксировано здесь как базовый вариант; допускается реализация с табами при явном согласовании с System Analyst.
5. Диспатч по `click-action` реализовать через **таблицу обработчиков** (map/dictionary: `{ 'show-animation': fn, 'open-spec': fn, 'both': fn }`), а не через `switch-case` или цепочку `if/else`.

---

## Ограничения

- `click-action` обрабатывается исключительно через map-диспатч — ключи словаря соответствуют значениям поля, значения словаря — функции-обработчики.
- Запрещено хардкодить пути к PNG или markdown-файлам. Все пути берутся из `card-meta.json`.
- Запрещено хардкодить `entityId` в логике обработчиков.

---

## Критерии приёмки

- [ ] Клик по карточке с `click-action: show-animation` показывает `AnimationPreview` с корректным PNG.
- [ ] Клик по карточке с `click-action: open-spec` раскрывает `SpecPanel` с отрендеренным markdown.
- [ ] Клик по карточке с `click-action: both` показывает оба компонента одновременно.
- [ ] Смена значения `click-action` в `card-meta.json` меняет поведение без изменения JS-кода.
- [ ] Нет хардкода `entityId` или файловых путей в коде (CI-grep-проверка пройдена).
- [ ] Диспатч реализован через map-словарь (код-ревью).
