# Системный аналитик — Life of T

## Роль и skill-файл

Роль: **System Analyst**  
Skill-файл: `system-analyst-skill.md`

---

## Инструкция перед началом

Перечитай все разделы `system-analyst-skill.md`:
- Раздел 1 (роль и позиция),
- Раздел 2 (зона ответственности),
- Раздел 3 (ограничения — НЕЛЬЗЯ),
- Раздел 4 (директории и артефакты),
- Раздел 5 (SDD-процесс),
- Раздел 6 (техспеки),
- Раздел 7 (Task Board),
- Раздел 9 (взаимодействие с ролями),
- Раздел 10 (чеклист).

Резюмируй вслух свои обязанности и ограничения, затем приступай к задаче.

---

## Контекст фичи: «Визуальный документационный сайт Life of T»

### Что произошло

Принято решение переработать визуальную составляющую игры «Life of T» и реализовать **документационный сайт**, сгенерированный из тех же данных, что и игровые ассеты. Сайт должен:

1. Визуально отображать всех персонажей, локации, UI-компоненты игры через спрайты, сгенерированные генератором ассетов (`ru.lifegame.assets`).
2. Быть собранным автоматически в GitHub Actions из XML-спек и данных bash-скриптов — без участия человека.
3. Строго соблюдать главный архитектурный принцип проекта: **никакого хардкода имён, идентификаторов и путей в коде и CI-логике** — только итерация по данным из манифестов.
4. Содержать интерактивные карточки с реакцией на клики (показ анимации, раскрытие спецификации).

### Текущее состояние репозитория (апрель 2026)

- **Генератор ассетов** (`asset-generator/`) работает: XML-спека → PNG + sprite-atlas.json. Поддерживаемые типы сущностей: `characters`, `locations`, `furniture`, `ui`. Архитектура: Clean Architecture + DDD, Java 21.
- **Пайплайн CI** (`.github/workflows/asset-generation.yml`, `asset-generator-ci.yml`) работает: генерирует спрайты и валидирует XML-спеки.
- **Каталог спек** (`game-content/life-of-t/src/main/resources/asset-specs/specs-manifest.xml`): 12 персонажей, 12 локаций, 18 предметов, 3 UI-группы — **единственный источник истины о составе сущностей**.
- **Схема** (`docs/prompts/_core/unified-asset-schema.xml`): описывает формат visual-specs.xml (meta, layers, color-palette, animations, time-of-day-variations, constraints).
- **Чего нет**: директории `scripts/`, `docs/visual-specs/`, нового output-mode `docs-preview` в генераторе, workflow для docs-site, bash-скриптов сбора данных, самого сайта.
- **Критическое нарушение** в существующем CI: в `.github/workflows/asset-generation.yml` захардкожен список ожидаемых PNG (`tanya_idle.png`, `sam_idle.png`, …). Это нарушение принципа независимости от конкретных данных — подлежит исправлению в рамках этой работы.

### Ключевые принципы (обязательны для всех задач)

1. **Никакого хардкода сущностей в коде, CI и логике сайта.** Список персонажей, локаций и компонентов — только из `specs-manifest.xml` (читается скриптом или генератором).
2. **Генератор не знает имён.** Он обходит манифест итерационно. Добавить персонажа = добавить строку в XML + создать спек. Сайт и CI обновятся автоматически.
3. **Документационные спеки — отдельный namespace.** `docs/visual-specs/` — никогда не смешивать с `game-content/.../asset-specs/`.
4. **Сайт — pure static + data-driven.** Данные приходят только из JSON-артефактов, собранных скриптами. Никакой серверной части.
5. **Все решения фиксируются в `docs/decisions/`.** Ни одной «устной договорённости».

### Карта промтов для системного аналитика

Выполняй промты строго в указанном порядке — каждый следующий зависит от артефактов предыдущего:

| № | Файл промта | SDD-фаза | Выходной артефакт |
|---|-------------|----------|-------------------|
| 0 | `00-visual-docs-site-overview.md` | — | Этот файл (контекст) |
| 1 | `01-adr-data-independence.md` | Specify | `docs/decisions/ADR-001-visual-docs-data-independence.md` |
| 2 | `02-techspec-docs-preview-mode.md` | Specify | `docs/specs/technical/visual-docs-preview-mode.md` |
| 3 | `03-techspec-docs-site-structure.md` | Specify | `docs/specs/technical/visual-docs-site-structure.md` |
| 4 | `04-techspec-ci-docs-workflow.md` | Specify | `docs/specs/technical/visual-docs-ci-workflow.md` |
| 5 | `05-techspec-ci-fix-hardcode.md` | Specify | `docs/specs/technical/ci-fix-asset-hardcode.md` |
| 6 | `06-tasks-backend.md` | Task | `tasks/backend/TASK-BE-DOC-*.md` |
| 7 | `07-tasks-frontend.md` | Task | `tasks/frontend/TASK-FE-DOC-*.md` |
| 8 | `08-tasks-assets.md` | Task | `tasks/assets/TASK-AS-DOC-*.md` |
| 9 | `09-tasks-testing.md` | Task | `tasks/testing/TASK-TEST-DOC-*.md` |
| 10 | `10-narrator-visual-specs-brief.md` | Task | `tasks/narrative/TASK-NR-DOC-*.md` |

---

> **Ограничение роли:** не выполняй работу Java Developer, JavaScript Developer и Нарратора. Твой выход — только спецификации, решения и задачи.
