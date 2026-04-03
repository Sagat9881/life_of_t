# TASK-BE-DOC-002: Расширение unified-asset-schema.xml секцией `docs-rendering`

**Идентификатор:** TASK-BE-DOC-002  
**Тип задачи:** assets  
**Статус:** TODO  
**Приоритет:** High  

---

## Контекст

Для корректной генерации карточек документационного сайта XML-спеки сущностей должны опционально содержать секцию `<docs-rendering>` с метаданными для превью. Системный аналитик определяет формат секции; Java Developer обновляет схему и добавляет поддержку парсинга.

> Принцип независимости зафиксирован в `docs/decisions/ADR-001-visual-docs-data-independence.md`. Секция является опциональной — отсутствие секции не должно ломать парсинг.

---

## Связанные спецификации

| Артефакт | Описание |
|---|---|
| `docs/specs/technical/visual-docs-preview-mode.md` | Поля `card-meta.json` и их источники |
| `docs/prompts/_core/unified-asset-schema.xml` | Текущая схема XML-спек (только чтение для Java Developer) |
| `docs/decisions/ADR-001-visual-docs-data-independence.md` | Принцип независимости |

---

## Что нужно сделать

### 1. Обновить `docs/prompts/_core/unified-asset-schema.xml`

Добавить опциональную секцию `<docs-rendering>` со следующими полями:

| Поле | Тип | Default | Описание |
|---|---|---|---|
| `caption` | `string` | `entity-id` | Локализованное название для карточки документационного сайта |
| `preview-animation` | `string` (animation id) | первая анимация в спеке | Id анимации, кадр которой используется для `preview.png` |
| `click-action` | `show-animation` \| `open-spec` \| `both` | `show-animation` | Действие при клике на карточку в docs-сайте |

Пример в XML-спеке сущности:

```xml
<docs-rendering>
  <caption>Татьяна — idle</caption>
  <preview-animation>idle</preview-animation>
  <click-action>both</click-action>
</docs-rendering>
```

### 2. Создать value object `DocRenderingConfig`

Пакет: `ru.lifegame.assets.domain.model` (или `domain/value` согласно принятой структуре пакетов).

Поля:
- `String caption` — из `<caption>`, default = entity id.
- `String previewAnimationId` — из `<preview-animation>`, default = `null` (означает «первая анимация»).
- `ClickAction clickAction` — enum: `SHOW_ANIMATION`, `OPEN_SPEC`, `BOTH`; default = `SHOW_ANIMATION`.

`ClickAction` — системный enum, не содержит имён конкретных сущностей.

### 3. Обновить XML-парсер

Место: `infrastructure/parser/` (существующий парсер XML-спек).

- При наличии секции `<docs-rendering>` — заполнить `DocRenderingConfig` из XML.
- При отсутствии секции — применить значения по умолчанию (не бросать исключение).
- Некорректное значение `click-action` — логировать предупреждение и применять default `SHOW_ANIMATION`.

### 4. Интеграция с TASK-BE-DOC-001

`DocRenderingConfig` должен быть доступен через `AssetSpec` (или соответствующую доменную модель) и использоваться в `GenerateDocsPreviewUseCase` при формировании `card-meta.json`.

---

## Архитектурные ограничения (из `java-developer-skill.md`, раздел 5)

- `ClickAction` enum — системный тип, не содержит имён персонажей или анимаций.
- Никакого хардкода значений полей `docs-rendering` в коде парсера или usecase.
- Парсер не знает, какие анимации существуют — `previewAnimationId` передаётся как непрозрачная строка.

---

## Критерии приёмки

- [ ] `docs/prompts/_core/unified-asset-schema.xml` содержит документированную секцию `<docs-rendering>` со всеми тремя полями, их типами и значениями по умолчанию.
- [ ] Парсер корректно читает секцию и заполняет `DocRenderingConfig`.
- [ ] При отсутствии секции в XML — применяются значения по умолчанию (парсинг не падает).
- [ ] Тесты парсера покрывают:
  - Секция присутствует, все поля заполнены — проверить маппинг каждого поля;
  - Секция отсутствует — проверить, что применены defaults;
  - `click-action` содержит неизвестное значение — default `SHOW_ANIMATION`, в логах предупреждение.
- [ ] `DocRenderingConfig` используется в `GenerateDocsPreviewUseCase` (TASK-BE-DOC-001) при генерации `card-meta.json`.

---

## Зависимости

- Блокирует полную реализацию **TASK-BE-DOC-001** (поле `card-meta.json` из `<docs-rendering>`). Рекомендуется выполнять параллельно или первой.
