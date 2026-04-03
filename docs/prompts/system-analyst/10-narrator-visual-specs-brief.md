# Промт 10 — Задача для Нарратора: визуальные спеки документационного сайта

## Роль и skill-файл

Роль: **System Analyst**  
Skill-файл: `system-analyst-skill.md`

---

## Инструкция перед началом

Перечитай `system-analyst-skill.md` (разделы 2, 5, 7, 9). Прочитай:
- `narrantor-skill.md` — ограничения и зона ответственности Нарратора;
- `docs/decisions/ADR-001-visual-docs-data-independence.md`;
- `docs/visual-specs/README.md` (создан в TASK-AS-DOC-002);
- `docs/visual-specs/_template/visual-specs.xml` — шаблон спеки.

Резюмируй обязанности и ограничения, затем создавай задачу.

---

## Задача

Создай задачу для Нарратора в `tasks/narrative/TASK-NR-DOC-001.md`.

---

### TASK-NR-DOC-001: Создание визуальных спек для документационного сайта

Файл: `tasks/narrative/TASK-NR-DOC-001.md`

**Контекст:**  
Проект реализует документационный сайт, который визуально отображает персонажей, локации и UI-компоненты игры «Life of T». Для каждой не-abstract сущности из `specs-manifest.xml` нужна документационная визуальная спека в `docs/visual-specs/`. Именно эти спеки читает генератор ассетов в режиме `docs-preview` для создания PNG-превью и метаданных карточек сайта.

Нарратор отвечает за **содержательное** заполнение спек: описание персонажа/локации для документации, выбор подходящей анимации для превью, указание действия при клике. Технический формат XML фиксирован `unified-asset-schema.xml` и не изменяется Нарратором.

**Тип задачи:** narrative

**Связанные спецификации:**
- `docs/visual-specs/README.md` — структура директории;
- `docs/visual-specs/_template/visual-specs.xml` — шаблон для заполнения;
- `docs/prompts/_core/unified-asset-schema.xml` — формат XML (только чтение);
- `docs/specs/technical/visual-docs-preview-mode.md` — что будет сгенерировано из спеки;
- `docs/decisions/ADR-001` — принцип независимости;
- `game-content/life-of-t/src/main/resources/asset-specs/specs-manifest.xml` — полный список сущностей (источник истины).

**Что нужно сделать:**

Для каждой не-abstract сущности из `specs-manifest.xml` создать файл `docs/visual-specs/{entityType}/{entityId}/visual-specs.xml` на основе шаблона `_template/visual-specs.xml`. В каждой спеке заполнить:

1. `<meta>`: `entity-type`, `entity-name`, `version`.
2. `<layers>`: минимум один слой с описанием визуального плана (словами — не код, не HTML).
3. `<color-palette>` (опционально): основные цвета персонажа/локации для документации.
4. `<docs-rendering>`:
   - `<caption>`: краткое название на русском для карточки сайта (например, «Татьяна — главная героиня»).
   - `<preview-animation>`: id анимации из игровой спеки (`idle` или другой подходящий для превью).
   - `<click-action>`: одно из `show-animation`, `open-spec`, `both` — что показывать при клике по карточке.
5. `<animations>`: описание ключевых анимаций для документации (не все — только те, что важны для понимания персонажа/локации).

**Приоритет сущностей** (начать с самых важных для документации):
- Первая очередь: все `characters/` (12 персонажей).
- Вторая очередь: все `locations/` (12 локаций).
- Третья очередь: `furniture/` и `ui/` группы.

**Ограничения для Нарратора (из `narrantor-skill.md`):**
- Не изменять технический формат XML — только содержательные поля.
- Не создавать спеки в `game-content/` — только в `docs/visual-specs/`.
- Не определять технические параметры (fps, frame-width, bit-depth) — эти значения берутся из игровых спек.
- `<caption>` — короткое, не длиннее 60 символов. Никакого лирического текста в XML.
- Все `<click-action>` значения — строго из допустимого набора: `show-animation` | `open-spec` | `both`.

**Критерии приёмки:**
- [ ] Для каждой не-abstract сущности из `specs-manifest.xml` создан файл `docs/visual-specs/{entityType}/{entityId}/visual-specs.xml`.
- [ ] Все файлы проходят xmllint (`xmllint --noout`).
- [ ] Все `<click-action>` — допустимые значения.
- [ ] Все `<caption>` — не длиннее 60 символов.
- [ ] CI-шаг `validate-docs-specs` (TASK-BE-DOC-003) проходит без ошибок после добавления спек.
- [ ] Нарратор не трогал файлы в `game-content/` или `asset-generator/`.

---

## Дополнительно: финальный чеклист системного аналитика по всей фиче

После создания всех задач (промты 01–10) системный аналитик проверяет по разделу 10 `system-analyst-skill.md`:

- [ ] ADR-001 создан и подписан.
- [ ] Все 4 техспеки созданы (`visual-docs-preview-mode.md`, `visual-docs-site-structure.md`, `visual-docs-ci-workflow.md`, `ci-fix-asset-hardcode.md`).
- [ ] Задачи созданы для всех ролей:
  - Java Developer: TASK-BE-DOC-001, TASK-BE-DOC-002, TASK-BE-DOC-003.
  - JavaScript Developer: TASK-FE-DOC-001, TASK-FE-DOC-002, TASK-FE-DOC-003.
  - Assets/DevOps: TASK-AS-DOC-001, TASK-AS-DOC-002.
  - Testing: TASK-TEST-DOC-001, TASK-TEST-DOC-002.
  - Нарратор: TASK-NR-DOC-001.
- [ ] Каждая задача содержит: ID, тип, ссылки на спеки, описание, критерии приёмки.
- [ ] Ни одна задача не требует «додумывать» поведение — всё описано в спеках.
- [ ] Принцип ADR-001 (нет хардкода) явно упомянут или применён в каждой задаче.
- [ ] Нарратор, Java Developer и JS Developer могут приступить к работе независимо (нет неразрешённых зависимостей).
