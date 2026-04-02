# TASK-BE-022 — Bash-скрипты сбора метрик

| Поле | Значение |
|------|----------|
| **ID** | TASK-BE-022 |
| **Тип** | backend |
| **Статус** | TODO |
| **Приоритет** | HIGH |
| **Роль** | Java Developer |
| **Дата** | 2026-04-02 |

## Описание

Реализовать bash-скрипты сбора метрик и GitHub Actions workflow YAML для пайплайна `build-status-dashboard`.

## Связанные спецификации

- `docs/specs/PIPELINE_SPEC.md` — полная спецификация (ОБЯЗАТЕЛЬНО прочитать перед реализацией)
- `docs/decisions/ADR-004-pipeline-publish-tool.md` — решение об инструменте публикации и формате скриптов

## Что нужно создать

1. `scripts/count-tasks.sh` — парсинг `tasks/**/*.md`, подсчёт по статусам (алгоритм в PIPELINE_SPEC.md §3.1)
2. `scripts/count-backend.sh` — подсчёт Java-классов по DDD-слоям (алгоритм в §3.2)
3. `scripts/count-narrative.sh` — подсчёт XML-спецификаций по директориям (алгоритм в §3.3)
4. `scripts/fetch-ci-status.sh` — запрос к GitHub API check-runs (алгоритм в §3.4)
5. `scripts/write-metrics.sh` — формирование `metrics.json` из переменных окружения (схема в §4.1)
6. `.github/workflows/build-status-dashboard.yml` — YAML workflow с jobs: collect-metrics, collect-assets, build-html, deploy

## Ограничения

- Все скрипты — bash (`.sh`), инструменты: `find`, `grep`, `wc`, `jq` (предустановлены на `ubuntu-latest`)
- YAML использует `peaceiris/actions-gh-pages@v3` (ADR-004)
- `continue-on-error: true` ТОЛЬКО на шагах: `fetch-ci-status`, `build-asset-generator`, `run-asset-generator`
- Permissions `contents: write` только на job-уровне `deploy`
- НЕ добавлять `continue-on-error` к: `checkout`, `write-metrics-json`, `inject-data-into-html`, `deploy-to-gh-pages`

## Критерии приёмки

- [ ] Все 5 bash-скриптов созданы в `scripts/`
- [ ] Каждый скрипт протестирован локально на примере репозитория
- [ ] `scripts/count-tasks.sh` возвращает `0` если `tasks/` пуста (не exit code 1)
- [ ] `scripts/fetch-ci-status.sh` возвращает `CI_STATUS=unknown` при ошибке сети
- [ ] YAML workflow создан в `.github/workflows/build-status-dashboard.yml`
- [ ] Workflow запускается при пуше в `main`, `workflow_dispatch`, и по расписанию `0 6 * * *`
- [ ] Все 4 jobs присутствуют с правильными `needs` зависимостями

## Метрики

После реализации: метрика «количество задач DONE» будет отображаться на Dashboard.
