# TASK-BE-022 — Bash-скрипты сбора метрик (tasks, backend, narrative, CI)

**Тип:** backend  
**Приоритет:** HIGH  
**Статус:** TODO  
**Роль:** Java Developer  
**Связанная спецификация:** `docs/specs/PIPELINE_SPEC.md` (разделы 2.1, 3.1–3.5)  
**Зависимость:** нет (может стартовать сразу)

## Описание

Реализовать bash-скрипты сбора метрик и GitHub Actions workflow для job `collect-metrics`.

## Что реализовать

1. **`scripts/ci/count-tasks.sh`** — алгоритм по разделу 3.1 спецификации.
2. **`scripts/ci/count-backend.sh`** — алгоритм по разделу 3.2.
3. **`scripts/ci/count-narrative.sh`** — алгоритм по разделу 3.3.
4. **`scripts/ci/fetch-ci-status.sh`** — алгоритм по разделу 3.4; `continue-on-error: true`.
5. **`scripts/ci/write-metrics.sh`** — алгоритм по разделу 3.5.
6. **`.github/workflows/build-dashboard.yml`** — только jobs `collect-metrics` и `deploy` (skeleton); permissions по разделу 5.2.

## Контракты выходных данных

Все скрипты обязаны:
- Завершаться с кодом `0` при любом состоянии данных.
- Записывать JSON в `$GITHUB_ENV`.
- Использовать `jq` для формирования JSON (доступен на ubuntu-latest).

## Критерии приёмки

- [ ] Все 5 скриптов проходят `shellcheck` без ошибок
- [ ] `count-tasks.sh` корректно считает задачи из `tasks/**/*.md` текущего репозитория
- [ ] `count-tasks.sh` возвращает нули при пустой `tasks/`
- [ ] `fetch-ci-status.sh` при недоступном API возвращает `"lastBuildStatus": "unknown"`
- [ ] `write-metrics.sh` создаёт `artifacts/metrics.json` валидный по схеме SPEC-SA-011
- [ ] `.github/workflows/build-dashboard.yml` содержит `permissions: pages: write, id-token: write`
- [ ] Workflow запускается по триггерам: push[main], workflow_dispatch, schedule(0 6 * * *)
