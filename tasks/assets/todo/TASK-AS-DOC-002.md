# TASK-AS-DOC-002: Создание структуры docs/visual-specs с шаблонным README

## Метаданные

| Поле              | Значение                                         |
|-------------------|--------------------------------------------------|
| **ID**            | TASK-AS-DOC-002                                  |
| **Тип**           | assets                                           |
| **Статус**        | todo                                             |
| **Исполнитель**   | Java Developer / DevOps                          |
| **Приоритет**     | средний                                          |
| **Создана**       | 2026-04-03                                       |
| **Спринт**        | документационный пайплайн                        |

---

## Контекст

Для документационного сайта нужен отдельный namespace `docs/visual-specs/`, **строго изолированный** от игровых спек в `game-content/`. Это разграничение обосновано в ADR-001: документационный рендеринг и игровой пайплайн должны иметь независимые источники данных.

Нарратор будет добавлять в `docs/visual-specs/` файлы `visual-specs.xml` для каждого персонажа и локации, которые отображаются на документационном сайте. Задача исполнителя — создать структуру директории и шаблон, которым будет пользоваться Нарратор.

---

## Связанные спецификации

- [`docs/decisions/ADR-001-visual-docs-data-independence.md`](../../../docs/decisions/ADR-001-visual-docs-data-independence.md) — почему `docs/visual-specs/` нельзя смешивать с `game-content/`;
- [`docs/specs/technical/visual-docs-preview-mode.md`](../../../docs/specs/technical/visual-docs-preview-mode.md) — формат секции `<docs-rendering>` в спеке;
- [`docs/prompts/_core/unified-asset-schema.xml`](../../../docs/prompts/_core/unified-asset-schema.xml) — базовый формат XML-спеки ассета (мета, слои, палитра).

---

## Что нужно сделать

### 1. Создать `docs/visual-specs/README.md`

Файл README должен содержать следующие разделы:

1. **Назначение директории**
   - Объяснить, что `docs/visual-specs/` — пространство для документационных спек.
   - Явно указать: эти спеки используются только документационным сайтом, не игровым пайплайном.

2. **Структура поддиректорий**
   - Описать шаблон пути: `docs/visual-specs/{entityType}/{entityId}/visual-specs.xml`.
   - Привести пример структуры (без реальных сущностей — только `{entityType}/{entityId}`).

3. **Формат спеки**
   - Ссылка на [`docs/prompts/_core/unified-asset-schema.xml`](../../../docs/prompts/_core/unified-asset-schema.xml) как на описание базового формата.
   - Ссылка на [`docs/specs/technical/visual-docs-preview-mode.md`](../../../docs/specs/technical/visual-docs-preview-mode.md) — для понимания секции `<docs-rendering>`.
   - Указать, что для создания новой спеки нужно скопировать `_template/visual-specs.xml`.

4. **Архитектурное обоснование**
   - Ссылка на [`docs/decisions/ADR-001-visual-docs-data-independence.md`](../../../docs/decisions/ADR-001-visual-docs-data-independence.md).
   - Кратко: данная директория изолирована от `game-content/` намеренно; смешивать запрещено.

5. **Кто добавляет спеки**
   - Нарратор (роль Narrator) создаёт файлы `visual-specs.xml` по задачам вида TASK-NR-DOC-*.
   - Исполнители задач assets/backend не создают реальные спеки в этой директории.

---

### 2. Создать `docs/visual-specs/_template/visual-specs.xml`

Шаблонная XML-спека — минимально валидный файл, служащий образцом для Нарратора.

**Требования к содержимому шаблона:**

```
Обязательные секции:
1. <meta>
   - entityType: "_template"
   - entityId: "_template"
   - displayName: "Template Entity"
   - description: placeholder-текст
   - abstract: false

2. <layers>
   - Минимум один <layer> с заглушкой (stub=true или пустой src):
     - id: "base"
     - type: "sprite" или "background"
     - src: "" (пустая строка — заглушка)
     - zIndex: 0

3. <color-palette>
   - Пустой элемент или с одним placeholder-цветом:
     <color id="primary" value="#000000" />

4. <docs-rendering>
   - Обязательное поле click-action (согласно visual-docs-preview-mode.md):
     <click-action type="modal" /> или аналог из спецификации
   - Остальные поля — placeholder-значения
```

**Шаблон должен:**
- Проходить валидацию `xmllint --noout`.
- Содержать XML-комментарии, объясняющие назначение каждой секции (для Нарратора).
- НЕ содержать имён конкретных персонажей или локаций.

---

### 3. Что НЕ нужно делать

- **Не создавать** реальные спеки для конкретных сущностей — это задача Нарратора (TASK-NR-DOC-001 и далее).
- **Не изменять** структуру `game-content/` — ADR-001 запрещает смешивание.
- **Не добавлять** `.gitkeep` — README уже обеспечивает присутствие директории в репозитории.

---

## Критерии приёмки

- [ ] `docs/visual-specs/README.md` создан, содержит описание назначения, структуры поддиректорий и ссылки на ADR-001, unified-asset-schema.xml и visual-docs-preview-mode.md.
- [ ] `docs/visual-specs/_template/visual-specs.xml` создан и является валидным XML (проходит `xmllint --noout`).
- [ ] Шаблон содержит секции `<meta>`, `<layers>` (с заглушкой), `<color-palette>` и `<docs-rendering>` с полем `click-action`.
- [ ] В шаблоне отсутствуют имена конкретных персонажей или локаций игры.
- [ ] Реальных спек конкретных сущностей в `docs/visual-specs/` нет (кроме `_template/`).
- [ ] `docs/visual-specs/` не пересекается структурно с `game-content/` — проверяется code review.

---

## Связанные задачи

- **Следующая:** TASK-NR-DOC-001 — Нарратор создаёт первые реальные `visual-specs.xml` по данному шаблону.
- **Зависит от:** ADR-001 (уже должен быть создан), `visual-docs-preview-mode.md` (уже должна быть создана).
