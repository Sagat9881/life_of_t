# TASK-BE-DOC-001: Реализовать output-mode `docs-preview` в генераторе ассетов

| Поле | Значение |
|------|----------|
| **ID** | TASK-BE-DOC-001 |
| **Тип** | backend / assets |
| **Компонент** | `asset-generator/` (`ru.lifegame.assets`) |
| **Исполнитель** | Java Developer |
| **Приоритет** | Высокий |
| **Зависимости** | — |
| **Связанные спецификации** | [`docs/specs/technical/visual-docs-preview-mode.md`](../../../docs/specs/technical/visual-docs-preview-mode.md) |
| **ADR** | [ADR-001](../../../docs/decisions/ADR-001-visual-docs-data-independence.md) |

---

## Описание

Реализовать в генераторе ассетов режим `docs-preview`:
при запуске с `--output-mode=docs-preview` генератор читает `specs-manifest.xml`,
парсит XML-спеки каждой `abstract=false` сущности и сохраняет
`docs-preview.json` в `ASSET_OUTPUT_DIR`.

Список сущностей — только из манифеста. Никакого хардкода ID в Java-коде.

## Задачи реализации

1. Добавить конфигурацию `assets.output-mode` (enum: `standard`, `docs-preview`).
2. Создать `EntityDocsDescriptor` (domain DTO).
3. Реализовать `DocsPreviewUseCase` в слое Application.
4. Реализовать `DocsPreviewJsonWriterAdapter` в Infrastructure.
5. Подключить в Application module при `output-mode=docs-preview`.
6. Ответить на открытые вопросы из техспека (раздел 3 `ci-fix-asset-hardcode.md`).

## Критерии приёмки

- [ ] `docs-preview.json` содержит ровно N объектов, где N = кол-во `abstract=false` в манифесте.
- [ ] Каждый объект содержит все поля из FR-2 техспека.
- [ ] `standard`-режим работает без регрессий (CI зелёный).
- [ ] Время генерации JSON ≤ 5 секунд (50 сущностей).
- [ ] Нет хардкода ID сущностей в Java-коде (code review).
- [ ] Unit-тесты покрывают `DocsPreviewUseCase` (happy path + отсутствие XML-спека).
