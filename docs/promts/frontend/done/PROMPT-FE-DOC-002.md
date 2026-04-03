# PROMPT-FE-DOC-002: Реализовать панель деталей и анимацию спрайтов

> Ветка: `main`
> Статус: ✅ done
> Компонент: `docs-site/js/detail.js`, `docs-site/css/style.css`
> Исполнитель: JavaScript Developer

---

> **Ветка:** `main` — все изменения только в `main`.
> **Движение задачи:**
> 1. Перед началом: переместить `tasks/frontend/todo/TASK-FE-DOC-002.md` → `tasks/frontend/in_progress/TASK-FE-DOC-002.md`.
> 2. После выполнения: переместить `tasks/frontend/in_progress/TASK-FE-DOC-002.md` → `tasks/frontend/done/TASK-FE-DOC-002.md`. Промт перенести в `docs/promts/frontend/done/PROMPT-FE-DOC-002.md`.

---

## Роль и skill-файл

Ты — **JavaScript Developer** проекта «Лиф оф T». Строго следуй `javascript-developer-skill.md`.

Перед началом перечитай все разделы скилл-файла (разделы 2, 3, 4, 7, 8, 9). Резюмируй обязанности и ограничения.

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

---

## Жёсткие ограничения

- ❌ Никаких хардкодов имён сущностей в `detail.js`.
- ❌ Никаких `switch-case` / `if-else` по `entity.id` или именам.
- ❌ Не трогать `narrative/`, `tasks/`, `src/`.
- ✅ Все данные — только из объекта `entity`.
- ✅ Работа строго в ветке `main`.
