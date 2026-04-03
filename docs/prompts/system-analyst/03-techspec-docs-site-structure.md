# Промт 03 — Техспека: структура и поведение документационного сайта

## Роль и skill-файл

Роль: **System Analyst**  
Skill-файл: `system-analyst-skill.md`

---

## Инструкция перед началом

Перечитай `system-analyst-skill.md` (разделы 1, 2, 3, 5, 6, 9). Прочитай:
- `javascript-developer-skill.md` (разделы 1, 2, 3, 7, 8, 9) — ограничения JS Developer;
- `docs/specs/technical/visual-docs-preview-mode.md` — формат `card-meta.json` (результат промта 02);
- `docs/decisions/ADR-001-visual-docs-data-independence.md` — принцип независимости данных.

Резюмируй обязанности и ограничения, затем выполняй.

---

## Задача

Создай техническую спецификацию в `docs/specs/technical/visual-docs-site-structure.md`.

### Контекст

Документационный сайт — **чисто статический**, данные приходят из JSON-артефактов, собранных bash-скриптами в CI. Никакой серверной части. Никакого хардкода.

Сайт строится из двух источников данных:
1. `site/data/catalog.json` — каталог всех сущностей, собранный из `specs-manifest.xml` и `card-meta.json` скриптом `scripts/collect-manifest.sh`.
2. `site/data/docs.json` — структура документации, собранная из `docs/*.md` скриптом `scripts/collect-docs.sh`.

### Что должно быть в техспеке

1. **Архитектура сайта**:
   - Стек: чистый HTML/CSS/vanilla JS (без фреймворков), либо Astro/11ty как static site generator — зафиксируй выбор и обоснование. Приоритет — максимальная простота и минимум зависимостей.
   - Структура директорий сайта: `site/src/`, `site/data/`, `site/public/`, `site/dist/`.
   - Сайт читает `site/data/catalog.json` при загрузке и строит DOM итерационно по каталогу.

2. **Компоненты страницы** (описать каждый через его поведение и данные, а не через вёрстку):
   - **EntityCard**: карточка сущности. Данные: `entityId`, `entityType`, `caption`, `previewPath`. Реакция на клик: открывает `AnimationPreview` для этой сущности.
   - **AnimationPreview**: показывает sprite-atlas.png первой анимации сущности (берётся из `card-meta.json` → `previewPath`). Загружается при клике, не при загрузке страницы.
   - **SpecPanel**: раскрываемая панель с содержимым `specPath` (markdown → HTML). Открывается при двойном клике или отдельной кнопке карточки.
   - **FilterBar**: фильтрация по `entityType` (characters / locations / furniture / ui). Значения типов берутся из `catalog.json`, не из хардкода.
   - **DocSection**: секции из `docs.json` — навигация по разделам технической документации.

3. **Контракт данных**:
   - Полный формат `catalog.json`: структура верхнего уровня, поля каждой записи (entityId, entityType, caption, previewPath, animationCount, layerCount, specVersion).
   - Полный формат `docs.json`: структура (список разделов с title, slug, content или contentPath).

4. **Правила JS Developer** (обязательны, ссылка на `javascript-developer-skill.md`):
   - Никакого хардкода `entityType` в коде — только значения из `catalog.json`.
   - `FilterBar` строит список фильтров итерационно по `entityType`-значениям каталога.
   - Реакции на клики описываются через абстрактные обработчики событий, привязанные к `entityId` из данных.
   - Запрещены DOM/CSS-фреймворки (React, Vue); разрешён vanilla JS + минимальный CSS.

5. **Сценарии использования** (пошагово):
   - Пользователь открывает сайт → загружается `catalog.json` → рендерится сетка карточек.
   - Пользователь кликает по фильтру `characters` → карточки фильтруются по `entityType === "characters"`.
   - Пользователь кликает по карточке → `AnimationPreview` загружает PNG по `previewPath` и показывает его.
   - Пользователь двойным кликом открывает `SpecPanel` → загружается markdown из `specPath` и отображается как текст.

6. **Связанные артефакты**: `ADR-001`, `visual-docs-preview-mode.md`, `visual-docs-ci-workflow.md` (промт 04).
7. **Метрики готовности**: сайт корректно отображает все сущности из `catalog.json`; CI-проверка валидирует, что число карточек равно числу не-abstract сущностей в манифесте.
8. **Задачи**: укажи TASK-FE-DOC-001, TASK-FE-DOC-002, TASK-FE-DOC-003 как выходные задачи этой спеки.

### Критерии готовности

- [ ] Спека создана в `docs/specs/technical/visual-docs-site-structure.md`.
- [ ] Форматы `catalog.json` и `docs.json` описаны полностью.
- [ ] Для каждого компонента описаны данные и поведение (не вёрстка).
- [ ] Явно запрещён хардкод entityType/entityId согласно ADR-001.
- [ ] Пошаговые сценарии использования описаны.

> **Ограничение:** не пиши HTML/JS-код. Только контракты, поведение и требования.
