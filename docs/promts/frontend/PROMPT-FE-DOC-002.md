# PROMPT-FE-DOC-002: Реализовать панель деталей и анимацию спрайтов

> **Ветка:** `main` — все изменения только в `main`.
> **Движение задачи:**
> 1. Перед началом: переместить `tasks/frontend/todo/TASK-FE-DOC-002.md` → `tasks/frontend/in_progress/TASK-FE-DOC-002.md`.
> 2. После выполнения: переместить `tasks/frontend/in_progress/TASK-FE-DOC-002.md` → `tasks/frontend/done/TASK-FE-DOC-002.md`. Промт перенести в `docs/promts/frontend/done/PROMPT-FE-DOC-002.md`.

---

## Роль и skill-файл

Ты — **JavaScript Developer** проекта «Life of T». Строго следуй `javascript-developer-skill.md`.

Перед началом перечитай все разделы skill-файла (разделы 2, 3, 4, 7, 8, 9). Резюмируй обязанности и ограничения.

> ⚠️ Для анимации спрайтов в `docs-site/` допустимо использование `<canvas>` или `background-position` — это документационный инструмент, не игровой клиент. Принцип: никаких хардкодов имён сущностей, всё из данных объекта `entity`.

---

## Задача

**ID:** TASK-FE-DOC-002  
**Компонент:** `docs-site/js/detail.js`  
**Приоритет:** Средний  
**Зависимости:** TASK-FE-DOC-001  
**Связанные спецификации:** `docs/specs/technical/visual-docs-site-structure.md`  
**ADR:** `docs/decisions/ADR-001-visual-docs-data-independence.md`

## Контекст

Реализовать панель деталей, открывающуюся по клику на карточку: анимация спрайта из `sprite-atlas.png` и отображение JSON-спецификации сущности.

**Обязательно прочитай перед реализацией:**
- `docs/specs/technical/visual-docs-site-structure.md` — раздел про detail panel
- `docs/decisions/ADR-001-visual-docs-data-independence.md`

## Цель

Создать `docs-site/js/detail.js` с panel открывающейся по клику, анимацией через данные из `entity.animations` и graceful fallback при `spriteAtlasFile === null`.

## Ответ по SDD

### 1. State/Scene модель

Состояние панели:
```js
// state только из данных entity — никаких хардкодов
const detailState = {
  entity: null,       // текущая открытая сущность
  isOpen: false,
  animFrame: 0,
  animTimer: null
};
```

### 2. Данные из backend

Анимация берётся из полей объекта `entity`:
- `entity.spriteAtlasFile` — путь к атласу (или `null`)
- `entity.animations` — массив кадров с размерами (согласно FR-2 из `visual-docs-preview-mode.md`)

Все решения — только по данным из `entity`. Никаких проверок `if (entity.id === 'tanya_idle')`.

### 3. Разделение по слоям

| Метод | Слой | Задача |
|-------|------|--------|
| `openDetail(entity)` | state + render | открыть панель, заполнить данными |
| `renderSprite(entity)` | render | canvas/background-position анимация |
| `renderJson(entity)` | render | `<pre>JSON.stringify(entity, null, 2)</pre>` |
| `closeDetail()` | state | закрыть, очистить таймер |

### 4. Implement

**Критерии готовности (DoD):**
- [ ] Клик на карточку открывает панель без перезагрузки страницы.
- [ ] JSON-спецификация отображается корректно для всех типов сущностей.
- [ ] Анимация запускается если `entity.spriteAtlasFile !== null`.
- [ ] При `spriteAtlasFile === null` — placeholder без ошибок в console.
- [ ] Нет имён сущностей в `detail.js` (grep возвращает 0).

---

## Жёсткие ограничения

- ❌ Никаких хардкодов имён сущностей в `detail.js`.
- ❌ Никаких `switch-case` / `if-else` по `entity.id` или `entity.name`.
- ❌ Не трогать `narrative/`, `tasks/`, `src/`.
- ✅ Все данные — только из объекта `entity`.
- ✅ Работа строго в ветке `main`.
