# Промт 07 — Задачи для JavaScript Developer (docs-site frontend)

## Роль и skill-файл

Роль: **System Analyst**  
Skill-файл: `system-analyst-skill.md`

---

## Инструкция перед началом

Перечитай `system-analyst-skill.md` (разделы 2, 5, 7, 9). Убедись, что следующие спецификации созданы и прочитаны:
- `docs/decisions/ADR-001-visual-docs-data-independence.md`;
- `docs/specs/technical/visual-docs-site-structure.md`;
- `docs/specs/technical/visual-docs-ci-workflow.md`.

Резюмируй обязанности и ограничения, затем создавай задачи.

---

## Задача

Создай три задачи для JavaScript Developer в `tasks/frontend/`.

---

### TASK-FE-DOC-001: Статический сайт — каркас и data-driven рендер карточек

Файл: `tasks/frontend/TASK-FE-DOC-001.md`

**Контекст:**  
Документационный сайт строится как pure-static приложение. Все данные поступают из `site/data/catalog.json` и `site/data/docs.json`, собранных в CI bash-скриптами. Структура данных описана в `docs/specs/technical/visual-docs-site-structure.md`.

**Тип задачи:** frontend

**Связанные спецификации:**
- `docs/specs/technical/visual-docs-site-structure.md` — формат catalog.json, docs.json, компоненты;
- `docs/decisions/ADR-001-visual-docs-data-independence.md` — запрет хардкода;
- `javascript-developer-skill.md` — ограничения роли.

**Что нужно сделать:**
1. Создать директорию `site/` с структурой: `src/`, `data/` (туда CI кладёт JSON), `public/`, `dist/`.
2. Реализовать загрузку `catalog.json` при инициализации (fetch/import).
3. Итерационно (без хардкода entityType/entityId) рендерить **EntityCard** для каждой записи в каталоге.
4. Реализовать **FilterBar**: список фильтров строится из уникальных `entityType` значений каталога — никакого хардкода типов.
5. Фильтрация — клиентская, по полю `entityType` из данных.

**Ограничения (из `javascript-developer-skill.md` и ADR-001):**
- Никакого `if (type === 'characters')` или аналогичного.
- Стек: vanilla JS + vanilla CSS. Никаких React/Vue/Angular.
- Никакого DOM/CSS для игровых анимаций (здесь не применимо, но принцип «данные управляют рендером» — обязателен).

**Критерии приёмки:**
- [ ] При добавлении новой сущности в `catalog.json` карточка появляется автоматически без изменений JS-кода.
- [ ] FilterBar корректно отражает все `entityType` из каталога, включая новые.
- [ ] Нет хардкода `entityType` или `entityId` в коде (CI-grep-проверка).
- [ ] Сайт собирается командой `npm run build` в `site/`.

---

### TASK-FE-DOC-002: Реакции на клики — AnimationPreview и SpecPanel

Файл: `tasks/frontend/TASK-FE-DOC-002.md`

**Контекст:**  
Карточки документационного сайта должны реагировать на клики согласно полю `click-action` из `card-meta.json` (значения: `show-animation`, `open-spec`, `both`). Поведение определяется **данными**, а не кодом.

**Тип задачи:** frontend

**Связанные спецификации:**
- `docs/specs/technical/visual-docs-site-structure.md` — компоненты AnimationPreview, SpecPanel;
- `docs/specs/technical/visual-docs-preview-mode.md` — формат card-meta.json, поле click-action;
- `javascript-developer-skill.md` (разделы 3, 4);
- `ADR-001`.

**Что нужно сделать:**
1. Реализовать **AnimationPreview**: при клике на карточку с `click-action: show-animation` или `both` — загрузить PNG по `previewPath` из card-meta и отобразить. Загрузка — lazy (только при клике).
2. Реализовать **SpecPanel**: при `click-action: open-spec` или `both` — загрузить markdown из `specPath`, конвертировать в HTML (минимальный marked.js или аналог), показать в раскрываемой панели.
3. Обработчик кликов — универсальный, привязан к `entityId` из данных. Никакого `if (entityId === 'tanya') { ... }`.
4. При `click-action: both` — показать оба компонента одновременно (или с табами — выбрать и зафиксировать в комментарии к задаче).

**Ограничения:**
- `click-action` обрабатывается через таблицу диспатча (map/dictionary), не через `switch-case` по строковому значению из данных — разрешены только ключи handler-функций.
- Запрещено хардкодить пути к PNG или markdown-файлам.

**Критерии приёмки:**
- [ ] Клик по карточке с `click-action: show-animation` показывает AnimationPreview.
- [ ] Клик по карточке с `click-action: open-spec` раскрывает SpecPanel с markdown.
- [ ] Клик по карточке с `click-action: both` показывает оба компонента.
- [ ] Поведение определяется данными из `card-meta.json` — смена `click-action` в JSON меняет поведение без изменения кода.
- [ ] Нет хардкода entityId или путей в коде.

---

### TASK-FE-DOC-003: Asset-cache и предзагрузка превью

Файл: `tasks/frontend/TASK-FE-DOC-003.md`

**Контекст:**  
Сайт загружает PNG-превью по запросу (lazy). При активной навигации пользователя возможны повторные запросы одного и того же ресурса. Нужен клиентский asset-cache.

**Тип задачи:** frontend

**Связанные спецификации:**
- `docs/specs/technical/visual-docs-site-structure.md`;
- `javascript-developer-skill.md` (раздел 6 — управление ресурсами).

**Что нужно сделать:**
1. Реализовать asset-cache слой поверх `fetch`/`Image`: кеш по ключу `previewPath`, `ImageBitmap` или `HTMLImageElement`.
2. При повторном запросе одного `previewPath` — возвращать из кеша без сетевого запроса.
3. Реализовать простую стратегию вытеснения: при превышении N кешированных объектов (N из конфига, не хардкода) — вытеснять LRU.
4. Placeholder-изображение для состояния загрузки (нейтральное, без имён сущностей — просто CSS-спиннер или серый квадрат).

**Критерии приёмки:**
- [ ] Повторный клик по карточке не вызывает сетевого запроса (проверяется через DevTools Network).
- [ ] Размер кеша ограничен параметром из конфига.
- [ ] Нет хардкода путей или имён в cache-логике.

---

> **Ограничение системного аналитика:** задачи созданы. JavaScript Developer читает их и реализует строго по спецификациям. Не пиши JS-код.
