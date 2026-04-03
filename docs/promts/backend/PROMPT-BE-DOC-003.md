# PROMPT-BE-DOC-003: Исправить хардкод в верификационных шагах CI

> **Ветка:** `main` — все изменения только в `main`.
> **Движение задачи:**
> 1. Перед началом: переместить `tasks/backend/todo/TASK-BE-DOC-003.md` → `tasks/backend/in_progress/TASK-BE-DOC-003.md`.
> 2. После выполнения: переместить `tasks/backend/in_progress/TASK-BE-DOC-003.md` → `tasks/backend/done/TASK-BE-DOC-003.md`. Промт перенести в `docs/promts/backend/done/PROMPT-BE-DOC-003.md`.

---

## Роль и skill-файл

Ты — **Java Developer** проекта «Life of T» (по части Java). YAML/bash-часть может выполнить любой участник. Строго следуй `java-developer-skill.md`.

Перед началом перечитай разделы 2, 5, 6, 7, 9, 10 skill-файла. Резюмируй обязанности и ограничения.

---

## Задача

**ID:** TASK-BE-DOC-003  
**Компонент:** `.github/workflows/asset-generation.yml`  
**Приоритет:** Высокий  
**Зависимости:** TASK-BE-DOC-002  
**Связанные спецификации:** `docs/specs/technical/ci-fix-asset-hardcode.md`  
**ADR:** `docs/decisions/ADR-001-visual-docs-data-independence.md`

## Контекст

В трёх шагах `asset-generation.yml` есть хардкод имён сущностей (`tanya`, `sam`, `bed`, `home_room`, `alexander`, `aijan` и др.), нарушающий ADR-001. Необходимо заменить на data-driven логику.

**Обязательно прочитай перед реализацией:**
- `docs/specs/technical/ci-fix-asset-hardcode.md` — раздел 2, описание трёх шагов замены
- `docs/decisions/ADR-001-visual-docs-data-independence.md`

## Цель

Заменить три хардкоженных шага CI на динамическую логику, читающую данные из манифеста/JSON/glob.

## Ответ по SDD

### 1. Specify

Три шага для рефакторинга:

**Шаг 1 — `Verify generated assets exist`:**
- Сейчас: bash-массив `EXPECTED` с хардкодом имён файлов.
- Должно быть: Python-скрипт, читающий сущности из `specs-manifest.xml` → генерирует список ожидаемых файлов динамически.

**Шаг 2 — `Validate atlas dimensions`:**
- Сейчас: Python-словарь с хардкодом размеров по именам сущностей.
- Должно быть: чтение размеров из `sprite-atlas.json` или XML-спека (согласовать формат с Java Developer).

**Шаг 3 — `Verify no anti-aliasing`:**
- Сейчас: список `sprites` с хардкодом имён.
- Должно быть: `glob *.png` — обходить все PNG без перечисления.

### 2. Plan

Последовательность:
1. Исправить шаг 3 (простейший — glob).
2. Исправить шаг 1 (Python + XML parsing).
3. Согласовать с Java Developer формат для шага 2, затем исправить.
4. Проверить CI локально через `act` или push в `main`.

### 3. Implement

**Критерии готовности (DoD):**
- [ ] `grep -n 'tanya\|sam\|bed\|home_room\|alexander\|aijan' .github/workflows/asset-generation.yml` возвращает 0 результатов (кроме комментариев).
- [ ] CI проходит для текущих 12 персонажей, 12 локаций, 18 предметов, 3 UI-групп.
- [ ] После добавления новой тестовой `<entity>` в манифест CI проходит без изменений YAML.

---

## Жёсткие ограничения

- ❌ Никаких новых хардкодов имён сущностей в YAML/bash/Python.
- ❌ Не трогать `narrative/`, `tasks/`, код в `src/`.
- ✅ Все три шага должны работать при любом составе сущностей в манифесте.
- ✅ Работа строго в ветке `main`.
