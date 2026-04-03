# TASK-FE-DOC-003: Создать CI workflow `docs-site.yml`

| Поле | Значение |
|------|----------|
| **ID** | TASK-FE-DOC-003 |
| **Тип** | frontend / devops |
| **Компонент** | `.github/workflows/docs-site.yml` |
| **Исполнитель** | JavaScript Developer |
| **Приоритет** | Высокий |
| **Статус** | 🔄 in_progress |

## Критерии приёмки

- [ ] Workflow завершается успешно через `workflow_dispatch`.
- [ ] Сайт доступен по URL GitHub Pages.
- [ ] Шаг `Verify no hardcoded entity names` проходит.
- [ ] push новой `<entity>` в манифест → сайт обновляется автоматически.
