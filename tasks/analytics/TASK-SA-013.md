# TASK-SA-013 — Спецификация GitHub Actions Pipeline

| Поле | Значение |
|------|----------|
| **ID** | TASK-SA-013 |
| **Тип** | analytics |
| **Статус** | DONE |
| **Приоритет** | HIGH |
| **Роль** | System Analyst |
| **Дата** | 2026-04-02 |

## Описание

Спроектировать техническую спецификацию GitHub Actions пайплайна `build-status-dashboard`, который автоматически собирает и публикует Project Status Dashboard при каждом пуше в `main`.

## Связанные спецификации

- `docs/specs/PIPELINE_SPEC.md` — основная спецификация (выходной артефакт)
- `docs/decisions/ADR-004-pipeline-publish-tool.md` — ADR о выборе инструмента публикации
- TASK-SA-010 (метрики), TASK-SA-011 (Overview), TASK-SA-012 (Assets Gallery) — зависимости

## Критерии приёмки

- [x] `docs/specs/PIPELINE_SPEC.md` создан и покрывает все 4 jobs
- [x] Bash-скрипты специфицированы (алгоритм + пример JSON)
- [x] ADR-004 задокументирован в `docs/decisions/`
- [x] Задачи TASK-BE-022, TASK-BE-023, TASK-FE-054, TASK-FE-055 созданы
- [x] Структура `output/` задокументирована
- [x] Способ настройки GitHub Pages описан

## Метрики

Затрагивает процессную метрику: «доля задач, выполненных без изменений спецификаций».
