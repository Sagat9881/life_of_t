# Промт 08 — Задачи для пайплайна ассетов (bash-скрипты и docs/visual-specs)

## Роль и skill-файл

Роль: **System Analyst**  
Skill-файл: `system-analyst-skill.md`

---

## Инструкция перед началом

Перечитай `system-analyst-skill.md` (разделы 2, 5, 7). Убедись, что следующие спецификации созданы:
- `docs/specs/technical/visual-docs-ci-workflow.md`;
- `docs/specs/technical/visual-docs-preview-mode.md`;
- `docs/decisions/ADR-001-visual-docs-data-independence.md`.

Резюмируй обязанности и ограничения, затем создавай задачи.

---

## Задача

Создай две задачи в `tasks/assets/`.

---

### TASK-AS-DOC-001: Реализация bash-скриптов сбора данных

Файл: `tasks/assets/TASK-AS-DOC-001.md`

**Контекст:**  
Документационный сайт получает данные из `site/data/catalog.json` и `site/data/docs.json`. Оба файла генерируются bash-скриптами в CI. Спецификации скриптов — в `docs/specs/technical/visual-docs-ci-workflow.md`.

**Тип задачи:** assets

**Связанные спецификации:**
- `docs/specs/technical/visual-docs-ci-workflow.md` — алгоритмы обоих скриптов;
- `docs/decisions/ADR-001-visual-docs-data-independence.md` — запрет хардкода;
- `game-content/life-of-t/src/main/resources/asset-specs/specs-manifest.xml` — структура манифеста.

**Что нужно сделать:**
1. Создать `scripts/collect-manifest.sh`:
   - Принимает аргументы: `<specs-manifest-path>` `<docs-preview-dir>` `<output-path>`.
   - Парсит `specs-manifest.xml` (python3 xml.etree или xmllint).
   - Для каждой не-abstract сущности читает `<docs-preview-dir>/<entityType>/<entityId>/card-meta.json`.
   - Собирает массив и записывает в `<output-path>` (catalog.json).
   - Никаких хардкодов имён. Пути — только из аргументов.
2. Создать `scripts/collect-docs.sh`:
   - Принимает аргументы: `<docs-dir>` `<output-path>`.
   - `find <docs-dir> -name '*.md' -not -path '*/prompts/*'` (исключить промты — только документация).
   - Для каждого файла: извлечь первый `# Заголовок`, slug из имени файла, записать `contentPath`.
   - Собрать JSON-массив, записать в `<output-path>` (docs.json).
3. Оба скрипта должны быть идемпотентны (повторный запуск перезаписывает выход).
4. Если `card-meta.json` не найден для сущности — скрипт завершается с кодом 1 и сообщением об ошибке (fail-fast).

**Критерии приёмки:**
- [ ] `scripts/collect-manifest.sh` создаёт корректный `catalog.json` для всех не-abstract сущностей манифеста.
- [ ] `scripts/collect-docs.sh` создаёт корректный `docs.json` для всех `.md` файлов в `docs/` (кроме `prompts/`).
- [ ] Оба скрипта не содержат имён персонажей, локаций или ассетов.
- [ ] При отсутствующем `card-meta.json` скрипт завершается с кодом 1.
- [ ] CI-шаг `collect-manifest` завершается успешно после шага `generate-docs-preview`.

---

### TASK-AS-DOC-002: Создание структуры docs/visual-specs с шаблонным README

Файл: `tasks/assets/TASK-AS-DOC-002.md`

**Контекст:**  
Для документационных спек нужен отдельный namespace `docs/visual-specs/`, строго изолированный от игровых спек в `game-content/`. Нарратор будет добавлять сюда `visual-specs.xml` для документационного отображения персонажей и локаций.

**Тип задачи:** assets

**Связанные спецификации:**
- `docs/decisions/ADR-001-visual-docs-data-independence.md`;
- `docs/specs/technical/visual-docs-preview-mode.md` — формат docs-rendering секции;
- `docs/prompts/_core/unified-asset-schema.xml` — базовый формат спек.

**Что нужно сделать:**
1. Создать директорию `docs/visual-specs/` с `README.md`, объясняющим:
   - Назначение директории (документационные спеки, отдельно от игровых).
   - Структуру поддиректорий: `docs/visual-specs/{entityType}/{entityId}/visual-specs.xml`.
   - Ссылку на `unified-asset-schema.xml` как на формат спеки.
   - Ссылку на ADR-001 — почему нельзя смешивать с игровыми спеками.
2. Создать шаблонный файл `docs/visual-specs/_template/visual-specs.xml` — валидная XML-спека с минимально заполненными полями (meta, layers с заглушкой, пустой color-palette, docs-rendering секция). Шаблон служит образцом для Нарратора.
3. Не создавать реальные спеки для конкретных сущностей — это задача Нарратора (TASK-NR-DOC-001).

**Критерии приёмки:**
- [ ] `docs/visual-specs/README.md` создан с описанием назначения, структуры и ссылками.
- [ ] `docs/visual-specs/_template/visual-specs.xml` — валидный XML, проходит xmllint.
- [ ] Шаблон содержит секцию `<docs-rendering>` с полем `click-action`.
- [ ] Реальных спек конкретных сущностей нет (не забегаем вперёд Нарратора).

---

> **Ограничение:** не пиши bash-код напрямую — только задачи с алгоритмическим описанием для исполнителя. Исполнитель TASK-AS-DOC-001 — Java Developer или DevOps-роль, реализующая bash-скрипты по спекам аналитика.
