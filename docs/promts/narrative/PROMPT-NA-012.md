# PROMPT-NA-012: [Narrative Task NA-012]

> **Ветка:** `main` — все изменения только в `main`.
> **Движение задачи:**
> 1. Перед началом: переместить `tasks/narrative/todo/TASK-NA-012.md` → `tasks/narrative/in_progress/TASK-NA-012.md`.
> 2. После выполнения: переместить `tasks/narrative/in_progress/TASK-NA-012.md` → `tasks/narrative/done/TASK-NA-012.md`. Промт перенести в `docs/promts/narrative/done/PROMPT-NA-012.md`.

---

## ⚠️ Внимание: задача требует уточнения + заблокирована

Файл `tasks/narrative/todo/TASK-NA-012.md` содержит только заглушку. Кроме того, TASK-NA-012 **заблокирована задачей TASK-NA-010** (`tatyana-facts.md`). 

**До начала выполнения необходимо:**
1. Убедиться, что TASK-NA-010 выполнена и `narrative/world/tatyana-facts.md` существует.
2. Найти оригинальное содержимое TASK-NA-012.
3. Восстановить полное описание в `tasks/narrative/todo/TASK-NA-012.md`.
4. Только после этого — приступать.

---

## Роль и skill-файл

Ты — **Нарратор** проекта «Life of T». Строго следуй `narrantor-skill.md`.

Перед началом перечитай все разделы skill-файла (разделы 1, 2, 3, 4, 6, 8). Резюмируй обязанности и ограничения.

---

## Шаблон выполнения (после разблокировки и восстановления задачи)

### Ответ по SDD

**1. Specify** *(ссылка: раздел 6.1 skill-файла)*
- Описать блок нарратива
- Опираться на `narrative/world/tatyana-facts.md` как на канонический источник фактов
- Артефакт: `narrative/specs/<block-id>/<entity_name>/spec.md`

**2. Plan** *(ссылка: раздел 6.2)*
- Артефакт: `narrative/plans/<block-name>-plan.md`

**3. Task**
- Передать план системному аналитику.

**4. Implement** *(ссылка: раздел 6.4)*
- `narrative/content/`
- `narrative/reviews/<block-name>-review.md`

---

## Жёсткие ограничения

- ❌ Нельзя начинать без выполненной TASK-NA-010.
- ❌ Нельзя делать грустный финал или менять реальные факты.
- ❌ Не изменять `src/`, `assets/`, `config/`, `tasks/`.
- ✅ Опираться на `narrative/world/tatyana-facts.md` как источник истины.
- ✅ Работа строго в ветке `main`.
