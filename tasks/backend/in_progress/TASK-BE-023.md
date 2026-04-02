# TASK-BE-023 — Bash-скрипт сканирования ассетов

| Поле | Значение |
|------|----------|
| **ID** | TASK-BE-023 |
| **Тип** | backend |
| **Статус** | TODO |
| **Приоритет** | HIGH |
| **Роль** | Java Developer |
| **Дата** | 2026-04-02 |

## Описание

Реализовать bash-скрипт `scripts/scan-assets.sh`, который обходит директорию `generated-assets/` и формирует `assets-manifest.json`.

## Связанные спецификации

- `docs/specs/PIPELINE_SPEC.md` §3.6 — алгоритм скрипта
- `docs/specs/PIPELINE_SPEC.md` §4.2 — контракт выходного `assets-manifest.json`
- `docs/decisions/ADR-004-pipeline-publish-tool.md` — решение о формате bash-скриптов

## Что нужно создать

`scripts/scan-assets.sh` — скрипт со следующим поведением:

1. `find generated-assets -name "*.png"` → список PNG
2. Для каждого PNG определить категорию по пути (`characters/`, `locations/`, `items/`, `ui/`)
3. Проверить наличие `sprite-atlas.json` рядом с PNG
4. Если атлас есть — прочитать поле `frames` через `jq`
5. Сформировать JSON с массивом `assets[]` (схема в §4.2)
6. Если `generated-assets/` пуста или не существует → `{"assets": [], "fallback": true, "totalAssets": 0, "generatedAt": "..."}`
7. Записать `assets-manifest.json`

## Ограничения

- Инструменты: bash, `find`, `jq` (предустановлены на `ubuntu-latest`)
- Скрипт НЕ должен завершаться с ошибкой (exit code 1) если `generated-assets/` отсутствует
- Поле `fallback: true` обязательно при пустом/отсутствующем `generated-assets/`
- Поле `path` в записях ассетов — относительный путь от корня `output/` (формат: `assets/category/filename.png`)

## Критерии приёмки

- [ ] `scripts/scan-assets.sh` создан
- [ ] При пустом `generated-assets/` скрипт завершается с exit code 0 и пишет fallback-манифест
- [ ] Поле `id` = имя файла без расширения (например, `tatyana-idle` из `tatyana-idle.png`)
- [ ] Поле `category` определяется корректно по первому сегменту пути после `generated-assets/`
- [ ] Если `sprite-atlas.json` отсутствует — `hasAtlas: false`, `frames: []`
- [ ] Выходной JSON валиден (проверить через `jq . assets-manifest.json`)

## Метрики

После реализации: метрика «количество сгенерированных ассетов» будет отображаться в Assets Gallery.
