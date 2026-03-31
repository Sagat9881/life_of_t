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

1. `scripts/ci/count-tasks.sh` — алгоритм по разделу 3.1
2. `scripts/ci/count-backend.sh` — алгоритм по разделу 3.2
3. `scripts/ci/count-narrative.sh` — алгоритм по разделу 3.3
4. `scripts/ci/fetch-ci-status.sh` — алгоритм по разделу 3.4
5. `scripts/ci/write-metrics.sh` — алгоритм по разделу 3.5
6. `.github/workflows/build-dashboard.yml` — jobs `collect-metrics` и `deploy` (skeleton)

## Критерии приёмки

- [ ] Все 5 скриптов проходят `shellcheck` без ошибок
- [ ] `count-tasks.sh` корректно считает задачи из `tasks/**/*.md`
- [ ] `fetch-ci-status.sh` при недоступном API возвращает `"lastBuildStatus": "unknown"`
- [ ] `write-metrics.sh` создаёт `artifacts/metrics.json` валидный по схеме
- [ ] Workflow запускается по триггерам: push[main], workflow_dispatch, schedule(0 6 * * *)
