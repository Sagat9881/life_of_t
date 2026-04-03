# Промт 06 — Задачи для Java Developer (backend / asset-generator)

## Роль и skill-файл

Роль: **System Analyst**  
Skill-файл: `system-analyst-skill.md`

---

## Инструкция перед началом

Перечитай `system-analyst-skill.md` (разделы 2, 5, 7). Убедись, что следующие спецификации созданы и прочитаны:
- `docs/decisions/ADR-001-visual-docs-data-independence.md`;
- `docs/specs/technical/visual-docs-preview-mode.md`;
- `docs/specs/technical/ci-fix-asset-hardcode.md`.

Резюмируй обязанности и ограничения, затем создавай задачи.

---

## Задача

Создай три задачи для Java Developer в `tasks/backend/`.

---

### TASK-BE-DOC-001: Новый output-mode `docs-preview` в AssetGeneratorRunner

Файл: `tasks/backend/TASK-BE-DOC-001.md`

**Контекст:**  
Проект переходит к генерации документационного сайта. Для этого генератор ассетов должен уметь работать в режиме `docs-preview`, создавая PNG-превью и JSON-метаданные для каждой не-abstract сущности из манифеста. Детали — в `docs/specs/technical/visual-docs-preview-mode.md`.

**Тип задачи:** assets / backend

**Связанные спецификации:**
- `docs/specs/technical/visual-docs-preview-mode.md` — основная спека;
- `docs/decisions/ADR-001-visual-docs-data-independence.md` — принцип независимости;
- `game-content/life-of-t/src/main/resources/asset-specs/specs-manifest.xml` — источник списка сущностей;
- `docs/prompts/_core/unified-asset-schema.xml` — формат XML-спек.

**Что нужно сделать:**
1. Добавить поддержку системного свойства `output.mode` в `AssetGeneratorRunner` (значение `docs-preview`).
2. В режиме `docs-preview`: для каждой не-abstract сущности из манифеста сгенерировать `preview.png` (128×128, TYPE_INT_ARGB) — первый кадр первого слоя или idle-анимации.
3. Сгенерировать `card-meta.json` рядом с `preview.png` — формат по спеке `visual-docs-preview-mode.md`.
4. Список сущностей получать **только из манифеста** — никакого хардкода имён.
5. Abstract-сущности (`abstract="true"`) пропускать.
6. Новый режим не должен затрагивать основной output-mode — реализовать как отдельную ветку в существующей луковой архитектуре.

**Ограничения (из `java-developer-skill.md`):**
- Никаких `if/switch` по именам персонажей или анимаций.
- Никакого хардкода путей к ассетам.
- Новый usecase в `application/`, новый infrastructure-адаптер в `infrastructure/generator/docs/`.
- Манифест читается через существующий `AssetSpecRepository` — не новый сканнер.

**Критерии приёмки:**
- [ ] Запуск `mvn ... -Doutput.mode=docs-preview` генерирует PNG + JSON для всех не-abstract сущностей из манифеста.
- [ ] Добавление новой сущности в манифест (без изменений кода) приводит к появлению нового `preview.png` и `card-meta.json`.
- [ ] Тесты покрывают: корректный размер PNG (128×128), наличие всех полей в `card-meta.json`, пропуск abstract-сущностей.
- [ ] Нет хардкода имён сущностей в коде (проверяется grep по именам персонажей из манифеста).

---

### TASK-BE-DOC-002: Расширение unified-asset-schema.xml секцией `docs-rendering`

Файл: `tasks/backend/TASK-BE-DOC-002.md`

**Контекст:**  
Для корректной генерации карточек документационного сайта XML-спеки сущностей должны опционально содержать секцию `<docs-rendering>` с метаданными для превью. Системный аналитик определяет формат; Java Developer обновляет схему и добавляет поддержку парсинга.

**Тип задачи:** assets

**Связанные спецификации:**
- `docs/specs/technical/visual-docs-preview-mode.md` — поля `card-meta.json`;
- `docs/prompts/_core/unified-asset-schema.xml` — текущая схема (только чтение для Java Developer);
- `ADR-001`.

**Что нужно сделать:**
1. Добавить в `docs/prompts/_core/unified-asset-schema.xml` опциональную секцию `<docs-rendering>` с полями: `caption` (локализованное название для карточки), `preview-animation` (id анимации для превью, default = первый), `click-action` (`show-animation` | `open-spec` | `both`).
2. Обновить XML-парсер в `infrastructure/parser/` для чтения новой секции в `DocRenderingConfig` value object.
3. Использовать `DocRenderingConfig` в TASK-BE-DOC-001 при генерации `card-meta.json`.

**Критерии приёмки:**
- [ ] `unified-asset-schema.xml` содержит секцию `<docs-rendering>` с описанием всех полей.
- [ ] Парсер корректно читает секцию; если секция отсутствует — применяются значения по умолчанию.
- [ ] Тесты парсера покрывают: наличие секции, отсутствие секции (defaults), некорректные значения `click-action`.

---

### TASK-BE-DOC-003: Обновление `asset-generator-ci.yml` — шаг validate-docs-specs

Файл: `tasks/backend/TASK-BE-DOC-003.md`

**Контекст:**  
Существующий CI (`asset-generator-ci.yml`) валидирует XML-спеки в `docs/prompts/`. Нужно добавить аналогичную валидацию для документационных спек в `docs/visual-specs/`.

**Тип задачи:** assets / testing

**Связанные спецификации:**
- `docs/specs/technical/visual-docs-ci-workflow.md`;
- `docs/decisions/ADR-001`;
- `.github/workflows/asset-generator-ci.yml` — текущий CI для изучения.

**Что нужно сделать:**
1. В `asset-generator-ci.yml` добавить job `validate-docs-specs`:
   - xmllint-проверка всех `docs/visual-specs/**/*.xml`.
   - Динамическая проверка: для каждой не-abstract сущности из `specs-manifest.xml` существует соответствующий файл в `docs/visual-specs/`. Список — из манифеста, не из хардкода.
2. Job не должен содержать имён конкретных сущностей.

**Критерии приёмки:**
- [ ] Job добавлен в `asset-generator-ci.yml`.
- [ ] Добавление сущности без создания `docs/visual-specs/` файла приводит к провалу CI.
- [ ] Добавление сущности с корректным файлом — CI проходит без изменения YAML.

---

> **Ограничение системного аналитика:** задачи созданы. Java Developer читает задачи как «только чтение» инструкций от аналитика. Не начинай реализацию — это вне роли аналитика.
