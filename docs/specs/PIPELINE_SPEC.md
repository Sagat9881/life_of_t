# PIPELINE_SPEC — Build Status Dashboard Pipeline

## §1. Назначение

Пайплайн `build-status-dashboard` автоматически собирает метрики проекта
и публикует HTML-дашборд на GitHub Pages.

## §2. Триггеры

| Триггер | Условие |
|---|---|
| push | ветка `main` |
| workflow_dispatch | ручной запуск |
| schedule | `0 6 * * *` (06:00 UTC ежедневно) |

## §3. Алгоритмы сбора метрик

### §3.1 count-tasks.sh

1. Принять аргумент `$1` — путь к директории задач (default: `tasks`).
2. Если директория отсутствует — вывести все переменные `=0`, выйти с кодом 0.
3. Найти все файлы `*.md` через `find <dir> -name '*.md'`.
4. Если файлов нет — вывести все переменные `=0`, выйти с кодом 0.
5. Для каждого статуса `TODO | IN_PROGRESS | REVIEW | DONE | BLOCKED` —
   использовать `grep -rl` по паттерну `| <STATUS>\b` или `**Статус** <STATUS>`.
6. Вывести переменные:
   ```
   TASKS_TODO=N
   TASKS_IN_PROGRESS=N
   TASKS_REVIEW=N
   TASKS_DONE=N
   TASKS_BLOCKED=N
   TASKS_TOTAL=N
   ```

### §3.2 count-backend.sh

1. Принять аргумент `$1` — путь к исходникам backend (default: `backend/src/main/java`).
2. Найти поддиректории `domain`, `application`, `infrastructure`, `presentation`.
3. Для каждого слоя — `find <layer-dir> -name '*.java' | wc -l`.
4. Вывести переменные:
   ```
   BACKEND_DOMAIN=N
   BACKEND_APPLICATION=N
   BACKEND_INFRASTRUCTURE=N
   BACKEND_PRESENTATION=N
   BACKEND_TOTAL=N
   ```

### §3.3 count-narrative.sh

1. Принять аргумент `$1` — путь к игровому контенту (default: `game-content`).
2. Поддиректории: `quests`, `npcs`, `world-events`, `conflicts`.
3. Для каждой — `find <dir> -name '*.xml' | wc -l`.
4. Вывести переменные:
   ```
   NARRATIVE_QUESTS=N
   NARRATIVE_NPCS=N
   NARRATIVE_WORLD_EVENTS=N
   NARRATIVE_CONFLICTS=N
   NARRATIVE_TOTAL=N
   ```

### §3.4 fetch-ci-status.sh

1. Взять переменные: `REPO_OWNER`, `REPO_NAME`, `GH_TOKEN`, `CI_REF` (SHA коммита).
2. Если `GH_TOKEN` пуст — вывести `CI_STATUS=unknown`, выйти 0.
3. Запрос: `GET /repos/{owner}/{repo}/commits/{ref}/check-runs`
   (заголовки: Authorization, Accept, X-GitHub-Api-Version).
4. При ошибке сети (`curl -sf` возвращает пустоту) — вывести `CI_STATUS=unknown`.
5. Агрегация:
   - Если есть `in_progress` → `CI_STATUS=in_progress`
   - Если есть `failure/timed_out/cancelled` → `CI_CONCLUSION=failure`
   - Если все `success` → `CI_CONCLUSION=success`
6. Вывести:
   ```
   CI_STATUS=...
   CI_CONCLUSION=...
   CI_TOTAL_RUNS=N
   ```

### §3.5 write-metrics.sh

Сборка `metrics.json` из переменных окружения. См. §4.1 для схемы.

### §3.6 scan-assets.sh

1. Принять `ASSETS_DIR` (default: `generated-assets`) и `OUTPUT_FILE` (default: `assets-manifest.json`) из окружения.
2. Если `ASSETS_DIR` отсутствует или пуст — записать fallback-манифест и выйти 0.
3. `find "$ASSETS_DIR" -name '*.png' | sort` — найти все PNG.
4. Для каждого PNG:
   a. `ID` = имя файла без расширения.
   b. `category` = первый сегмент пути после `ASSETS_DIR/` через `cut -d'/' -f1`.
   c. `path` = `assets/<category>/<filename>.png` (относительно от `output/`).
   d. Проверить `<dir>/sprite-atlas.json`:
      - Есть → `hasAtlas: true`, `frames` = `.frames` через `jq`.
      - Нет → `hasAtlas: false`, `frames: []`.
5. Сборка массива через `jq --argjson entry ... '. + [$entry]'`.
6. Записать выходной JSON через `jq -n`.

## §4. Схемы данных

### §4.1 metrics.json

```json
{
  "generated_at": "ISO-8601",
  "tasks": {
    "todo": 0, "in_progress": 0, "review": 0,
    "done": 0, "blocked": 0, "total": 0
  },
  "backend": {
    "domain": 0, "application": 0, "infrastructure": 0,
    "presentation": 0, "total": 0
  },
  "narrative": {
    "quests": 0, "npcs": 0, "world_events": 0,
    "conflicts": 0, "total": 0
  },
  "ci": {
    "status": "unknown", "conclusion": "unknown", "total_runs": 0
  }
}
```

### §4.2 assets-manifest.json

```json
{
  "assets": [
    {
      "id":       "tatyana-idle",
      "category": "characters",
      "path":     "assets/characters/tatyana-idle.png",
      "hasAtlas": true,
      "frames":   ["frame_0", "frame_1"]
    }
  ],
  "fallback":     false,
  "totalAssets":  1,
  "generatedAt":  "2026-04-02T06:00:00Z"
}
```

**Fallback (пустой или отсутствующий `generated-assets/`):**

```json
{
  "assets":      [],
  "fallback":    true,
  "totalAssets": 0,
  "generatedAt": "ISO-8601"
}
```

**Поля:**

| Поле | Тип | Описание |
|---|---|---|
| `id` | string | Имя файла без `.png` |
| `category` | string | `characters` / `locations` / `items` / `ui` / `unknown` |
| `path` | string | `assets/<category>/<file>.png` (отн. от `output/`) |
| `hasAtlas` | boolean | `true` если `sprite-atlas.json` есть рядом |
| `frames` | array | Содержимое `.frames` из атласа, либо `[]` |
| `fallback` | boolean | `true` при отсутствии/пустоте `generated-assets/` |
| `totalAssets` | number | Общее количество PNG |
| `generatedAt` | string | ISO-8601 UTC момент запуска |

## §5. Jobs и зависимости

```
collect-metrics ──┐
                  ├──► build-html ──► deploy
collect-assets  ──┘
```

| Job | needs | permissions |
|---|---|---|
| collect-metrics | — | default |
| collect-assets | — | default |
| build-html | collect-metrics, collect-assets | default |
| deploy | build-html | contents: write |

## §6. continue-on-error

Применяется ТОЛЬКО к шагам:
- `fetch-ci-status`
- `build-asset-generator`
- `run-asset-generator`

НЕ применяется к: `checkout`, `write-metrics-json`, `inject-data-into-html`, `deploy-to-gh-pages`.
