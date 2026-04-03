# TASK-BE-DOC-001: Новый output-mode `docs-preview` в AssetGeneratorRunner

**Идентификатор:** TASK-BE-DOC-001  
**Тип задачи:** assets / backend  
**Статус:** TODO  
**Приоритет:** High  

---

## Контекст

Проект переходит к генерации документационного сайта. Для этого генератор ассетов должен уметь работать в режиме `docs-preview`, создавая PNG-превью и JSON-метаданные для каждой не-abstract сущности из манифеста.

> Принцип независимости данных зафиксирован в `docs/decisions/ADR-001-visual-docs-data-independence.md`. Все решения по формату выходных артефактов — в `docs/specs/technical/visual-docs-preview-mode.md`.

---

## Связанные спецификации

| Артефакт | Описание |
|---|---|
| `docs/specs/technical/visual-docs-preview-mode.md` | **Основная спека** — формат `preview.png`, `card-meta.json`, правила выбора кадра |
| `docs/decisions/ADR-001-visual-docs-data-independence.md` | Принцип независимости от конкретных сущностей |
| `game-content/life-of-t/src/main/resources/asset-specs/specs-manifest.xml` | Источник списка всех сущностей |
| `docs/prompts/_core/unified-asset-schema.xml` | Формат XML-спек (только чтение) |

---

## Что нужно сделать

1. **Добавить поддержку системного свойства `output.mode`** в `AssetGeneratorRunner`.
   - Допустимое значение: `docs-preview`.
   - При отсутствии свойства — работа в штатном режиме без изменений.

2. **В режиме `docs-preview`**: для каждой не-abstract сущности из манифеста:
   - Сгенерировать `preview.png` (128×128, `BufferedImage.TYPE_INT_ARGB`) — первый кадр первого слоя или idle-анимации (приоритет — idle, при отсутствии — первый кадр первого слоя).
   - Файл сохраняется рядом со спекой сущности: `<output-dir>/<entity-id>/preview.png`.

3. **Сгенерировать `card-meta.json`** рядом с `preview.png`:
   - Формат полей — строго по `docs/specs/technical/visual-docs-preview-mode.md`.
   - Если для сущности присутствует секция `<docs-rendering>` (TASK-BE-DOC-002) — использовать её поля; иначе — defaults.

4. **Список сущностей — только из манифеста** (`AssetSpecRepository`). Никакого хардкода имён.

5. **Abstract-сущности** (`abstract="true"`) — пропускать.

6. **Архитектура**: новый режим — отдельная ветка в луковой архитектуре:
   - Новый usecase: `application/usecase/GenerateDocsPreviewUseCase`.
   - Новый infrastructure-адаптер: `infrastructure/generator/docs/DocsPreviewGenerator`.
   - Основной pipeline генерации (`output.mode=default`) не затрагивается.

---

## Архитектурные ограничения (из `java-developer-skill.md`, раздел 5)

- Никаких `if/switch` по именам персонажей, анимаций, слоёв.
- Никакого хардкода путей к ассетам или именам файлов.
- Манифест читается только через существующий `AssetSpecRepository` — не новый файловый сканнер.
- Добавление новой сущности в манифест не требует изменений кода.

---

## Критерии приёмки

- [ ] Запуск `mvn ... -Doutput.mode=docs-preview` генерирует `preview.png` + `card-meta.json` для **всех** не-abstract сущностей из манифеста.
- [ ] Добавление новой сущности в манифест (без изменений кода) приводит к появлению нового `preview.png` и `card-meta.json`.
- [ ] Тесты покрывают:
  - корректный размер PNG: 128×128, `TYPE_INT_ARGB`;
  - наличие всех обязательных полей в `card-meta.json` (согласно спеке);
  - пропуск abstract-сущностей (`abstract="true"`).
- [ ] Нет хардкода имён сущностей: `grep` по именам персонажей из манифеста не находит совпадений в `src/`.
- [ ] Основной `output.mode` (default) не изменил поведения — регрессионные тесты проходят.

---

## Зависимости

- **TASK-BE-DOC-002** (рекомендуется выполнить первой или параллельно): секция `<docs-rendering>` в XML-спеках и `DocRenderingConfig` value object используются при формировании `card-meta.json`.
