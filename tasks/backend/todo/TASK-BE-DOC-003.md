# TASK-BE-DOC-003: Исправить хардкод в верификационных шагах CI

| Поле | Значение |
|------|----------|
| **ID** | TASK-BE-DOC-003 |
| **Тип** | backend / devops |
| **Компонент** | `.github/workflows/asset-generation.yml` |
| **Исполнитель** | Java Developer (по части Java) + любой (по части YAML/bash) |
| **Приоритет** | Высокий |
| **Зависимости** | TASK-BE-DOC-002 |
| **Связанные спецификации** | [`docs/specs/technical/ci-fix-asset-hardcode.md`](../../../docs/specs/technical/ci-fix-asset-hardcode.md) |
| **ADR** | [ADR-001](../../../docs/decisions/ADR-001-visual-docs-data-independence.md) |

---

## Описание

Заменить хардкоды в трёх шагах `asset-generation.yml` на data-driven логику
согласно спецификации исправлений (раздел 2 техспека `ci-fix-asset-hardcode.md`).

## Задачи реализации

1. Шаг `Verify generated assets exist` — заменить bash-массив `EXPECTED` на Python-скрипт,
   читающий сущности из `specs-manifest.xml`.
2. Шаг `Validate atlas dimensions` — заменить Python-словарь на чтение размеров из
   `sprite-atlas.json` или XML-спека (согласовать с Java Developer).
3. Шаг `Verify no anti-aliasing` — заменить список `sprites` на `glob *.png`.
4. Все три шага должны работать без изменений при добавлении новых сущностей.

## Критерии приёмки

- [ ] `grep -n 'tanya\|sam\|bed\|home_room\|alexander\|aijan' .github/workflows/asset-generation.yml` возвращает 0 результатов (кроме комментариев).
- [ ] CI проходит для текущих 12 персонажей, 12 локаций, 18 предметов, 3 UI-групп.
- [ ] После добавления новой тестовой `<entity>` в манифест (в рамках PR) CI проходит без изменений YAML.
