# TASK-BE-018 — Скрипт сбора метрик Task Board из `tasks/`

| Поле | Значение |
|------|----------|
| **ID** | TASK-BE-018 |
| **Тип** | backend |
| **Статус** | TODO |
| **Приоритет** | MEDIUM |
| **Роль** | Java Developer |
| **Эпик** | EPIC-DEVOPS-001 |
| **Связанная спека** | `docs/specs/PROJECT_METRICS_SPEC.md` §2.1 |
| **Зависит от** | TASK-SA-010 |

## Описание

Написать скрипт `scripts/metrics/collect_tasks.py`, который рекурсивно обходит директорию `tasks/` и собирает метрики группы **Task Board Health (TBH)**.

## Требования к скрипту

1. Читать все `.md`-файлы рекурсивно из `tasks/`
2. Парсить поля из таблицы в заголовке файла: `Статус`, `Тип`, `Зависит от`
3. Вычислять метрики TBH-01..TBH-05
4. Выводить результат в JSON согласно схеме из `PROJECT_METRICS_SPEC.md §4`
5. Записывать результат в `docs/status/taskboard_metrics.json`

## Формат выходного JSON

```json
{
  "total": 0, "done": 0, "inProgress": 0, "todo": 0,
  "blocked": 0, "readyToStart": 0, "completionPct": 0.0,
  "byRole": {
    "backend":   { "total": 0, "done": 0, "pct": 0.0 },
    "frontend":  { "total": 0, "done": 0, "pct": 0.0 },
    "assets":    { "total": 0, "done": 0, "pct": 0.0 },
    "narrative": { "total": 0, "done": 0, "pct": 0.0 }
  }
}
```

## Критерии приёмки

- [ ] Скрипт создан в `scripts/metrics/collect_tasks.py`
- [ ] Все метрики TBH-01..TBH-05 вычисляются корректно
- [ ] Выходной JSON соответствует схеме из `PROJECT_METRICS_SPEC.md §4`
- [ ] При отсутствии поля `Статус` — задача считается `TODO`, логируется предупреждение
- [ ] Скрипт не падает при пустой директории `tasks/`
