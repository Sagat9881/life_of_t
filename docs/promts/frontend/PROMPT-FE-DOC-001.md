# PROMPT-FE-DOC-001: Создать структуру директории `docs-site/` и `main.js`

> **Ветка:** `main` — все изменения только в `main`.
> **Движение задачи:**
> 1. Перед началом: переместить `tasks/frontend/todo/TASK-FE-DOC-001.md` → `tasks/frontend/in_progress/TASK-FE-DOC-001.md`.
> 2. После выполнения: переместить `tasks/frontend/in_progress/TASK-FE-DOC-001.md` → `tasks/frontend/done/TASK-FE-DOC-001.md`. Промт перенести в `docs/promts/frontend/done/PROMPT-FE-DOC-001.md`.

---

## Роль и skill-файл

Ты — **JavaScript Developer** проекта «Life of T». Строго следуй `javascript-developer-skill.md`.

Перед началом перечитай **все** разделы skill-файла, охватывая:
- Зону ответственности (раздел 2)
- Жёсткие ограничения НЕЛЬЗЯ (раздел 3) — особенно 3.1 (никаких хардкодов), 3.2 (только Canvas для игры, DOM допустим для docs-site), 3.3 (никакой нарративной логики)
- Структуру кода (раздел 7)
- Методологию SDD (раздел 8)
- Чеклист (раздел 9)

> ⚠️ **Важная особенность для задач docs-site:** `docs-site/` — это документационный статический сайт, **не** игровой Canvas-клиент. DOM/HTML/CSS здесь допустимы. Принцип data independence (ADR-001) сохраняется: никаких хардкодов имён сущностей в JS-коде.

Резюмируй: твои обязанности, что тебе запрещено, какие артефакты ты создаёшь.

---

## Задача

**ID:** TASK-FE-DOC-001  
**Компонент:** `docs-site/` (новая директория)  
**Приоритет:** Высокий  
**Зависимости:** TASK-BE-DOC-001 (нужен `docs-preview.json` для тестирования)  
**Связанные спецификации:** `docs/specs/technical/visual-docs-site-structure.md`  
**ADR:** `docs/decisions/ADR-001-visual-docs-data-independence.md`

## Контекст

Создать файловую структуру `docs-site/` и реализовать data-driven загрузку данных.

**Обязательно прочитай перед реализацией:**
- `docs/specs/technical/visual-docs-site-structure.md` — полная архитектура сайта
- `docs/decisions/ADR-001-visual-docs-data-independence.md`

## Цель

Создать скелет `docs-site/` с data-driven рендером карточек из `docs-preview.json`. Ни одного хардкода имён сущностей в JS.

## Ответ по SDD

### 1. State/Scene модель и структура

Структура файлов согласно техспеку:
```
docs-site/
  index.html          ← пустые контейнеры
  js/
    main.js           ← fetch('./data/docs-preview.json') → итерация → renderer
    renderer.js       ← createCard(entity) — только по полям объекта, без имён
    filter.js         ← фильтрация по полю entity.type
  css/
    style.css         ← базовая сетка карточек
  data/
    .gitkeep          ← JSON придёт от CI
```

### 2. Данные из backend API

- Источник данных: `docs-site/data/docs-preview.json` (генерируется TASK-BE-DOC-001).
- Структура объекта: определена в `docs/specs/technical/visual-docs-preview-mode.md` (FR-2).
- JS работает **только** с полями объекта (`entity.id`, `entity.type`, `entity.displayName` и т.д.) — никаких проверок по конкретным значениям.

### 3. Разделение по слоям

| Файл | Слой | Задача |
|------|------|--------|
| `main.js` | net + orchestration | fetch JSON, итерация, вызов renderer |
| `renderer.js` | render | createCard(entity) — DOM-карточка по полям |
| `filter.js` | state | фильтрация массива по `entity.type` |
| `style.css` | render | сетка, базовые стили |

### 4. Implement

**Критерии готовности (DoD):**
- [ ] При наличии `docs-site/data/docs-preview.json` сайт отображает N карточек.
- [ ] `grep -rn 'tanya\|sam\|aijan' docs-site/js/` возвращает 0 результатов.
- [ ] Фильтр по `type` работает (characters/locations/furniture/ui).
- [ ] `spriteAtlasFile === null` не вызывает ошибок в console.
- [ ] Сайт открывается через `npx serve docs-site` без 404.

---

## Жёсткие ограничения

- ❌ Никаких хардкодов имён сущностей (`tanya`, `sam` и т.п.) в JS.
- ❌ Никаких `switch-case` / `if-else` по `entity.id` или именам.
- ❌ Не трогать `narrative/`, `tasks/`, `src/`.
- ✅ Только данные из JSON управляют тем, что рендерится.
- ✅ Работа строго в ветке `main`.
