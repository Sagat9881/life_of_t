# TASK-BE-023 — Bash-скрипт сканирования ассетов → assets-manifest.json

**Тип:** backend  
**Приоритет:** HIGH  
**Статус:** TODO  
**Роль:** Java Developer  
**Связанная спецификация:** `docs/specs/PIPELINE_SPEC.md` (разделы 2.2, 3.6)  
**Зависимость:** нет (может стартовать сразу)

## Описание

Реализовать bash-скрипт `scan-assets.sh` и добавить job `collect-assets` в workflow.

## Что реализовать

1. `scripts/ci/scan-assets.sh` — алгоритм по разделу 3.6 `PIPELINE_SPEC.md`
2. Job `collect-assets` в `.github/workflows/build-dashboard.yml` согласно разделу 2.2
3. Fallback-манифест (раздел 2.2 спецификации)

## Критерии приёмки

- [ ] `scan-assets.sh` проходит `shellcheck` без ошибок
- [ ] При наличии `generated-assets/` — манифест содержит корректные `atlasPath`
- [ ] При отсутствии `generated-assets/` — записывается fallback с 5 пустыми категориями
- [ ] `metaPath: null` если `sprite-atlas.json` отсутствует
- [ ] Скрипт завершается кодом `0` в любом состоянии
- [ ] Манифест валиден по JSON Schema
