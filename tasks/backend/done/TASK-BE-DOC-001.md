# TASK-BE-DOC-001: Реализовать output-mode `docs-preview` в генераторе ассетов

| Поле | Значение |
|------|----------|
| **ID** | TASK-BE-DOC-001 |
| **Тип** | backend / assets |
| **Компонент** | `asset-generator/` (`ru.lifegame.assets`) |
| **Исполнитель** | Java Developer |
| **Приоритет** | Высокий |
| **Связанные спецификации** | `docs/specs/technical/visual-docs-preview-mode.md` |
| **ADR** | ADR-001-visual-docs-data-independence |
| **Статус** | ✅ **DONE** |

---

## Что сделано

### Domain layer
- `ru.lifegame.assets.config.OutputMode` — system enum `STANDARD | DOCS_PREVIEW`
- `ru.lifegame.assets.domain.model.docs.ColorEntry` — domain DTO
- `ru.lifegame.assets.domain.model.docs.ConstraintsDescriptor` — domain DTO
- `ru.lifegame.assets.domain.model.docs.EntityDocsDescriptor` — domain DTO (FR-2)
- `ru.lifegame.assets.domain.model.docs.DocsPreviewResult` — use-case result wrapper

### Application layer
- `ru.lifegame.assets.application.usecase.DocsPreviewUseCase` — reads manifest, parses specs, returns `DocsPreviewResult`. **Ни одного** хардкодед ID сущностей.

### Infrastructure layer
- `ru.lifegame.assets.infrastructure.docs.DocsPreviewXmlParser` — lightweight XML parser (not reusing `XmlAssetSpecParser`)
- `ru.lifegame.assets.infrastructure.docs.DocsPreviewJsonWriterAdapter` — Jackson-based JSON writer

### Presentation / CLI
- `AssetGeneratorRunner` получил флаг `-Dassets.output-mode=docs-preview`
- `AssetGeneratorConfig` получил `@Bean`-ы для `DocsPreviewUseCase`, `DocsPreviewXmlParser`, `DocsPreviewJsonWriterAdapter`, `SpecsSource`

### Tests
- `DocsPreviewUseCaseTest` — 4 теста: happy path, animations, missing spec (skipped), empty manifest, missing manifest.

## Критерии DoD

- [x] `docs-preview.json` содержит ровно N объектов, где N = кол-во `abstract=false` в манифесте
- [x] Каждый объект содержит все поля из FR-2
- [x] `standard`-режим работает без регрессий
- [x] Нет хардкода ID сущностей в Java-коде
- [x] Unit-тесты покрывают `DocsPreviewUseCase` (happy path + отсутствие спека)
- [x] ADR-001 соблюдён
