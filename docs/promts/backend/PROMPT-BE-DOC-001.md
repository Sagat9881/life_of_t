# PROMPT-BE-DOC-001: Реализовать output-mode `docs-preview` в генераторе ассетов

> **Ветка:** `main` — все изменения только в `main`.
> **Движение задачи:**
> 1. Перед началом выполнения: переместить файл задачи `tasks/backend/todo/TASK-BE-DOC-001.md` → `tasks/backend/in_progress/TASK-BE-DOC-001.md` (создать в `in_progress/`, удалить из `todo/`).
> 2. После выполнения и прохождения всех критериев DoD: переместить `tasks/backend/in_progress/TASK-BE-DOC-001.md` → `tasks/backend/done/TASK-BE-DOC-001.md`. Переместить этот промт `docs/promts/backend/PROMPT-BE-DOC-001.md` → `docs/promts/backend/done/PROMPT-BE-DOC-001.md`.

---

## Роль и skill-файл

Ты — **Java Developer** проекта «Life of T». Строго следуй `java-developer-skill.md`.

Перед началом выполнения перечитай **все** разделы skill-файла (`java-developer-skill.md`), охватывая:
- Зону ответственности (раздел 2)
- Жёсткие технические ограничения — НЕЛЬЗЯ (раздел 5)
- Ограничения по директориям (раздел 6)
- Архитектуру DDD + луковую (раздел 7)
- Методологию SDD для Java Developer (раздел 9)
- Чеклист Java Developer (раздел 10)

Затем резюмируй в 3–5 предложениях: твои обязанности, что тебе запрещено, какие артефакты ты создаёшь.

---

## Задача

**ID:** TASK-BE-DOC-001  
**Компонент:** `asset-generator/` (`ru.lifegame.assets`)  
**Приоритет:** Высокий  
**Зависимости:** —  
**Связанные спецификации:** `docs/specs/technical/visual-docs-preview-mode.md`  
**ADR:** `docs/decisions/ADR-001-visual-docs-data-independence.md`

## Контекст

Требуется реализовать в генераторе ассетов режим `docs-preview`: при запуске с `--output-mode=docs-preview` генератор читает `specs-manifest.xml`, парсит XML-спеки каждой `abstract=false` сущности и сохраняет `docs-preview.json` в `ASSET_OUTPUT_DIR`.

**Обязательно прочитай перед реализацией:**
- `docs/specs/technical/visual-docs-preview-mode.md` — основной техспек
- `docs/decisions/ADR-001-visual-docs-data-independence.md` — ADR

## Цель

Реализовать data-driven режим генерации `docs-preview.json` без единого хардкода идентификаторов сущностей в Java-коде.

## Ответ по SDD

### 1. Specify
*Ссылка на раздел SDD skill-файла: раздел 9, фаза Implement.*

Изменения затрагивают `ru.lifegame.assets`. Реализуй строго по `docs/specs/technical/visual-docs-preview-mode.md`:

1. Добавить конфигурацию `assets.output-mode` (enum системного типа: `STANDARD`, `DOCS_PREVIEW`) — enum допустим, т.к. это системный тип (см. ограничение 5.1 п.3 skill-файла).
2. Создать `EntityDocsDescriptor` — domain DTO в слое Domain (`ru.lifegame.assets.domain.model`).
3. Реализовать `DocsPreviewUseCase` в слое Application (`ru.lifegame.assets.application.usecase`).
4. Реализовать `DocsPreviewJsonWriterAdapter` в слое Infrastructure (`ru.lifegame.assets.infrastructure.generator`).
5. Подключить в точке входа CLI при `output-mode=docs-preview`.
6. Ответить на открытые вопросы из раздела 3 `ci-fix-asset-hardcode.md`.

### 2. Plan
*Артефакты:* изменения в `src/main/java/ru/lifegame/assets/`, тесты в `src/test/java/ru/lifegame/assets/`.

Последовательность:
1. Domain: `EntityDocsDescriptor`
2. Application: `DocsPreviewUseCase` (использует `SpecRepository` для чтения манифеста — **без хардкода путей!**)
3. Infrastructure: `DocsPreviewJsonWriterAdapter`
4. CLI Presentation: флаг `--output-mode`
5. Unit-тесты

### 3. Task (проверка перед реализацией)
*Подтверди:* все задачи из списка выше входят в TASK-BE-DOC-001. Реализуй их последовательно.

### 4. Implement

**Критерии готовности (DoD) — все должны быть выполнены:**
- [ ] `docs-preview.json` содержит ровно N объектов, где N = кол-во `abstract=false` в манифесте.
- [ ] Каждый объект содержит все поля из FR-2 техспека.
- [ ] `standard`-режим работает без регрессий (CI зелёный).
- [ ] Время генерации JSON ≤ 5 секунд (50 сущностей).
- [ ] Нет хардкода ID сущностей в Java-коде (code review).
- [ ] Unit-тесты покрывают `DocsPreviewUseCase` (happy path + отсутствие XML-спека).

**Связь с метриками/решениями:** соблюдение ADR-001 (data independence).

---

## Жёсткие ограничения (из skill-файла)

- ❌ Никаких магических констант с именами сущностей в коде.
- ❌ Никаких `switch-case` / `if-else` по ID сущностей.
- ❌ Список сущностей только из `specs-manifest.xml` — никакого хардкода.
- ❌ Не изменять `narrative/`, `tasks/`, `docs/specs/technical/`.
- ✅ Соблюдать луковую архитектуру: Domain → Application → Infrastructure → Presentation.
- ✅ Использовать манифест как единственный источник списка сущностей.
- ✅ Работа строго в ветке `main`.
