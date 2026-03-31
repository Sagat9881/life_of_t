# TASK-BE-023 — Bash-скрипт сканирования ассетов → assets-manifest.json

**Тип:** backend  
**Приоритет:** HIGH  
**Статус:** TODO  
**Роль:** Java Developer  
**Связанная спецификация:** `docs/specs/PIPELINE_SPEC.md` (разделы 2.2, 3.6); `docs/specs/STATUS_PAGE_ASSETS_SPEC.md` (раздел 2.1)  
**Зависимость:** нет (может стартовать сразу)

## Описание

Реализовать bash-скрипт `scan-assets.sh` и добавить job `collect-assets` в workflow.

## Что реализовать

1. **`scripts/ci/scan-assets.sh`** — алгоритм по разделу 3.6 `PIPELINE_SPEC.md`.
2. Добавить job `collect-assets` в `.github/workflows/build-dashboard.yml` согласно разделу 2.2.
3. Реализовать fallback-манифест (раздел 2.2 спецификации).

## Контракт выходных данных

`artifacts/assets-manifest.json` обязан проходить JSON Schema из `docs/specs/STATUS_PAGE_ASSETS_SPEC.md`, раздел 2.1.

## Критерии приёмки

- [ ] `scan-assets.sh` проходит `shellcheck` без ошибок
- [ ] При наличии `generated-assets/` — манифест содержит корректные `atlasPath`
- [ ] При отсутствии `generated-assets/` — записывается fallback с 5 пустыми категориями
- [ ] `metaPath: null` если `sprite-atlas.json` отсутствует
- [ ] `fileSizeBytes` корректно заполняется из размера PNG
- [ ] Скрипт завершается кодом `0` в любом состоянии файловой системы
- [ ] Манифест валиден по JSON Schema (SPEC-SA-012, раздел 2.1)
