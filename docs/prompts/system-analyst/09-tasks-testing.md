# Промт 09 — Задачи для тестирования и CI-рефакторинга

## Роль и skill-файл

Роль: **System Analyst**  
Skill-файл: `system-analyst-skill.md`

---

## Инструкция перед началом

Перечитай `system-analyst-skill.md` (разделы 2, 5, 7). Убедись, что следующие спецификации созданы:
- `docs/decisions/ADR-001-visual-docs-data-independence.md`;
- `docs/specs/technical/ci-fix-asset-hardcode.md`;
- `docs/specs/technical/visual-docs-ci-workflow.md`.

Резюмируй обязанности и ограничения, затем создавай задачи.

---

## Задача

Создай две задачи в `tasks/testing/`.

---

### TASK-TEST-DOC-001: Рефакторинг `asset-generation.yml` — устранение хардкода

Файл: `tasks/testing/TASK-TEST-DOC-001.md`

**Контекст:**  
В `.github/workflows/asset-generation.yml` три шага содержат захардкоженные имена ассетов (`tanya_idle.png`, `sam_idle.png`, …), что нарушает ADR-001. При добавлении персонажа без изменения YAML — новый ассет не проверяется (тихий провал). Полное описание нарушений — в `docs/specs/technical/ci-fix-asset-hardcode.md`.

**Тип задачи:** testing

**Связанные спецификации:**
- `docs/specs/technical/ci-fix-asset-hardcode.md` — алгоритм исправления;
- `docs/decisions/ADR-001-visual-docs-data-independence.md`;
- `.github/workflows/asset-generation.yml` — файл для рефакторинга.

**Что нужно сделать:**
1. Заменить шаг `Verify generated assets exist`: хардкодный массив → динамическое построение из `specs-manifest.xml` (python3 / xmllint — согласно спеке `ci-fix-asset-hardcode.md`).
2. Заменить шаг `Validate atlas dimensions`: ожидаемые размеры → из `card-meta.json` каждого ассета.
3. Заменить шаг `Verify no anti-aliasing`: список спрайтов → из манифеста.
4. Все замены должны быть **data-driven**: добавление новой сущности в `specs-manifest.xml` автоматически добавляет её в проверки.
5. Не менять логику валидации (PNG 32-bit, RGBA, no AA) — только источник списка проверяемых файлов.

**Критерии приёмки:**
- [ ] `asset-generation.yml` не содержит имён персонажей/локаций/ассетов (`grep -n 'tanya\|sam\|garfield' .github/workflows/asset-generation.yml` → пусто).
- [ ] Добавление тестовой сущности в `specs-manifest.xml` → CI корректно проверяет её ассеты без изменения YAML.
- [ ] Валидация PNG-формата (32-bit RGBA) сохраняется.
- [ ] Workflow не сломан — все существующие проверки проходят.

---

### TASK-TEST-DOC-002: CI-шаг `validate-catalog` — динамическая проверка полноты каталога

Файл: `tasks/testing/TASK-TEST-DOC-002.md`

**Контекст:**  
В новом workflow `docs-site.yml` нужен шаг, гарантирующий, что сгенерированный `catalog.json` содержит записи для **всех** не-abstract сущностей из `specs-manifest.xml`. Это предотвращает «тихое» выпадение сущностей из документационного сайта.

**Тип задачи:** testing

**Связанные спецификации:**
- `docs/specs/technical/visual-docs-ci-workflow.md` — описание шага validate-catalog;
- `docs/decisions/ADR-001-visual-docs-data-independence.md`;
- `docs/specs/technical/visual-docs-site-structure.md` — формат catalog.json.

**Что нужно сделать:**
1. Реализовать bash/python-скрипт (или inline в YAML) для шага `validate-catalog`:
   - Прочитать `specs-manifest.xml` → получить список не-abstract entityId.
   - Прочитать `site/data/catalog.json` → получить список entityId.
   - Проверить, что первый список является подмножеством второго.
   - При несоответствии — вывести список недостающих entityId и завершиться с кодом 1.
2. Шаг не должен содержать конкретных имён сущностей.
3. Встроить шаг в `docs-site.yml` после `collect-manifest`.

**Критерии приёмки:**
- [ ] При наличии не-abstract сущности в манифесте без записи в catalog.json — CI падает с информативным сообщением.
- [ ] При полном соответствии — CI проходит.
- [ ] Добавление новой сущности в манифест + генерация превью → validate-catalog проходит автоматически.
- [ ] Нет хардкода entityId в коде шага.

---

> **Ограничение системного аналитика:** задачи созданы. Не реализуй YAML или скрипты самостоятельно — это работа исполнителей задач.
