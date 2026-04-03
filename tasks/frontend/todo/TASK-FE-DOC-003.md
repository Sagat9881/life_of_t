# TASK-FE-DOC-003: Создать CI workflow `docs-site.yml`

| Поле | Значение |
|------|----------|
| **ID** | TASK-FE-DOC-003 |
| **Тип** | frontend / devops |
| **Компонент** | `.github/workflows/docs-site.yml` (новый файл) |
| **Исполнитель** | JavaScript Developer |
| **Приоритет** | Высокий |
| **Зависимости** | TASK-BE-DOC-001, TASK-BE-DOC-002, TASK-FE-DOC-001 |
| **Связанные спецификации** | [`docs/specs/technical/visual-docs-ci-workflow.md`](../../../docs/specs/technical/visual-docs-ci-workflow.md) |
| **ADR** | [ADR-001](../../../docs/decisions/ADR-001-visual-docs-data-independence.md) |

---

## Описание

Создать `.github/workflows/docs-site.yml` согласно архитектуре из техспека.
Workflow должен: генерировать `docs-preview.json` (или переиспользовать артефакт),
скачивать PNG-спрайты, собирать `docs-site/` и публиковать на GitHub Pages.

## Задачи реализации

1. Скопировать архитектурную схему из техспека в реальный YAML-файл.
2. Реализовать шаг `Verify no hardcoded entity names in JS`.
3. Настроить GitHub Pages для репозитория (если не настроено).
4. Протестировать workflow вручную через `workflow_dispatch`.

## Критерии приёмки

- [ ] Workflow завершается успешно через `workflow_dispatch`.
- [ ] Сайт доступен по URL GitHub Pages.
- [ ] Шаг `Verify no hardcoded entity names` проходит.
- [ ] Добавление новой `<entity>` в манифест и push в master → сайт автоматически обновляется.
