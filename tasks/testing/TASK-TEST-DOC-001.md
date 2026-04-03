# TASK-TEST-DOC-001: Написать интеграционный тест data independence

| Поле | Значение |
|------|----------|
| **ID** | TASK-TEST-DOC-001 |
| **Тип** | testing |
| **Компонент** | `.github/workflows/`, `docs-site/`, `asset-generator/` |
| **Исполнитель** | тестировщик-автоматизатор / System Analyst |
| **Приоритет** | Средний |
| **Зависимости** | TASK-BE-DOC-001, TASK-BE-DOC-003, TASK-FE-DOC-001, TASK-FE-DOC-003 |
| **Связанные спецификации** | [`docs/specs/technical/visual-docs-ci-workflow.md`](../../docs/specs/technical/visual-docs-ci-workflow.md), [ADR-001](../../docs/decisions/ADR-001-visual-docs-data-independence.md) |

---

## Описание

Ключевой интеграционный тест: добавить тестовую `<entity>` в `specs-manifest.xml`,
запустить CI и убедиться, что сайт и верификации обновляются **без изменений кода**.

## Задачи реализации

1. Разработать скрипт `scripts/test-data-independence.sh`:
   - Добавить тестовую `<entity path="characters/test_char" abstract="false"/>` в манифест.
   - Запустить `asset-generation.yml` через `workflow_dispatch`.
   - Проверить, что CI проходит.
   - Запустить `docs-site.yml`.
   - Проверить, что `docs-preview.json` содержит `test_char`.
   - Удалить тестовую сущность, восстановить манифест.

2. Добавить Playwright smoke-тест для `docs-site`:
   - Открыть сайт.
   - Проверить: `document.querySelectorAll('.entity-card').length === N` (из JSON).
   - Проверить: клик на первую карточку открывает панель деталей.

## Критерии приёмки

- [ ] Тест добавления/удаления сущности проходит без изменений YAML/JS.
- [ ] Playwright smoke завершается успешно.
- [ ] Тест включён в CI как опциональный шаг (не блокирует merge).
