# TASK-SA-013 — Спецификация GitHub Actions Pipeline

**Тип:** analytics  
**Приоритет:** HIGH  
**Статус:** DONE  
**Роль:** System Analyst  
**Связанная спецификация:** `docs/specs/PIPELINE_SPEC.md`  
**ADR:** `docs/decisions/ADR-003-pipeline-publish-tool.md`  
**Зависимость:** TASK-SA-010, TASK-SA-011, TASK-SA-012

## Описание

Создать техническую спецификацию GitHub Actions пайплайна, собирающего и публикующего Project Status Dashboard.

## Результат

- [x] Создан `docs/specs/PIPELINE_SPEC.md` с разделами 1–8
- [x] Специфицированы все 4 jobs: collect-metrics, collect-assets, build-html, deploy
- [x] Специфицированы 6 bash-скриптов с алгоритмами и контрактами
- [x] Задокументирована структура `output/`
- [x] Описана настройка GitHub Pages (Settings → Pages → Source: GitHub Actions)
- [x] Создан ADR-003 о выборе инструмента публикации
- [x] Задокументировано решение Bash vs Python
- [x] Созданы задачи TASK-BE-022, TASK-BE-023, TASK-FE-054, TASK-FE-055

## Метрики

Связанные метрики: время выполнения workflow (цель ≤ 10 мин), доступность страницы по целевому URL.
