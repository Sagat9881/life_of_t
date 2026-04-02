# PIPELINE_SPEC.md — GitHub Actions Pipeline: Project Status Dashboard

**ID:** TASK-SA-013  
**Версия:** 1.0  
**Дата:** 2026-04-02  
**Автор:** System Analyst (SDD §5 — Task-фаза)  
**Зависимости:** TASK-SA-010 (метрики), TASK-SA-011 (Overview), TASK-SA-012 (Assets Gallery)  
**Связанные решения:** `docs/decisions/ADR-004-pipeline-publish-tool.md`

---

## Содержание

1. [Архитектура пайплайна](#1-архитектура-пайплайна)
2. [Спецификация jobs](#2-спецификация-jobs)
3. [Спецификация bash-скриптов](#3-спецификация-bash-скриптов)
4. [Структура артефактов output/](#4-структура-артефактов-output)
5. [Конфигурация GitHub Pages](#5-конфигурация-github-pages)
6. [Стратегия обработки ошибок](#6-стратегия-обработки-ошибок)
7. [ADR](#7-adr)

---

## 1. Архитектура пайплайна

### 1.1. Контекст и цель

> **Ссылка на SDD §6 (Технические спецификации):** Каждая техническая спецификация содержит контекст, цель и связь с метриками проекта.

Пайплайн `build-status-dashboard` автоматически собирает метрики проекта и публикует HTML-страницу на GitHub Pages при каждом пуше в `main`. Это обеспечивает актуальное состояние проекта без ручного обновления документации.

**Цель:** сделать Project Status Dashboard (`https://sagat9881.github.io/life_of_t/`) единым окном видимости состояния «Life of T» — задачи, код, нарратив, ассеты.

### 1.2. Триггеры

| Триггер | Условие | Назначение |
|---------|---------|------------|
| `push: branches: [main]` | любой пуш в `main` | автоматическая актуализация |
| `workflow_dispatch` | ручной запуск | отладка, принудительное обновление |
| `schedule: cron: '0 6 * * *'` | ежедневно в 06:00 UTC | регулярное обновление даже без пушей |

### 1.3. Диаграмма jobs и зависимостей

```
workflow: build-status-dashboard
│
├── [job-1] collect-metrics
│   │  runner: ubuntu-latest
│   │  inputs: репозиторий (tasks/, backend/src/, game-content/, GitHub API)
│   │  outputs: metrics.json (artifact)
│   │
├── [job-2] collect-assets  ──needs: collect-metrics
│   │  runner: ubuntu-latest
│   │  inputs: asset-generator/pom.xml, game-content/assets/specs/
│   │  outputs: assets-manifest.json (artifact), generated-assets/*.png
│   │
├── [job-3] build-html  ─────needs: collect-metrics, collect-assets
│   │  runner: ubuntu-latest
│   │  inputs: metrics.json, assets-manifest.json, templates/dashboard.html
│   │  outputs: output/ (artifact)
│   │
└── [job-4] deploy  ─────────needs: build-html
       runner: ubuntu-latest
       inputs: output/ (artifact)
       outputs: gh-pages branch → https://sagat9881.github.io/life_of_t/
```

### 1.4. Нефункциональные требования

| Требование | Значение |
|-----------|---------|
| Runner | `ubuntu-latest` (бесплатный tier) |
| Платные credits | не требуются |
| Максимальное время выполнения | ≤ 15 минут |
| Permissions | `contents: write` (только для job `deploy`) |
| Secrets | `GITHUB_TOKEN` (встроенный, не требует настройки) |

---

## 2. Спецификация jobs

### 2.1. job: `collect-metrics`

**Назначение:** Собрать метрики состояния проекта из репозитория и GitHub API, записать в `metrics.json`.

**Переменные окружения:**

| Переменная | Источник | Назначение |
|-----------|---------|-----------|
| `GITHUB_TOKEN` | `secrets.GITHUB_TOKEN` | доступ к GitHub API для CI-статуса |
| `GITHUB_REPOSITORY` | автоматически | `owner/repo` для API-запросов |
| `GITHUB_SHA` | автоматически | SHA текущего коммита |

**Steps:**

| # | Имя шага | Действие | Входные файлы | Выходные данные |
|---|---------|---------|--------------|----------------|
| 1 | `checkout` | `actions/checkout@v4` | — | рабочая копия репозитория |
| 2 | `count-tasks` | `bash scripts/count-tasks.sh` | `tasks/**/*.md` | env: `TASKS_TODO`, `TASKS_IN_PROGRESS`, `TASKS_DONE` |
| 3 | `count-backend-classes` | `bash scripts/count-backend.sh` | `backend/src/**/*.java` | env: `BACKEND_AGGREGATES`, `BACKEND_SERVICES`, `BACKEND_CONTROLLERS`, `BACKEND_TESTS` |
| 4 | `count-narrative-specs` | `bash scripts/count-narrative.sh` | `game-content/**/*.xml` | env: `NARRATIVE_QUESTS`, `NARRATIVE_CONFLICTS`, `NARRATIVE_NPC`, `NARRATIVE_WORLD_EVENTS`, `NARRATIVE_WITH_MANIFEST` |
| 5 | `fetch-ci-status` | `bash scripts/fetch-ci-status.sh` | GitHub API (`/repos/{owner}/{repo}/commits/{sha}/check-runs`) | env: `CI_STATUS` (`success`/`failure`/`unknown`) |
| 6 | `write-metrics-json` | `bash scripts/write-metrics.sh` | все переменные выше | `metrics.json` |
| 7 | `upload-artifact` | `actions/upload-artifact@v4` | `metrics.json` | artifact `metrics-json` |

**Условие продолжения:** шаг 5 (`fetch-ci-status`) — `continue-on-error: true`. При недоступности API, `CI_STATUS=unknown`.

---

### 2.2. job: `collect-assets`

**Зависимость:** `needs: collect-metrics`

**Назначение:** Скомпилировать и запустить `asset-generator`, отсканировать сгенерированные PNG, записать `assets-manifest.json`.

**Переменные окружения:**

| Переменная | Значение | Назначение |
|-----------|---------|-----------|
| `JAVA_VERSION` | `17` | версия JDK для Maven |
| `ASSET_OUTPUT_DIR` | `generated-assets` | директория для PNG |

**Steps:**

| # | Имя шага | Действие | Входные файлы | Выходные данные |
|---|---------|---------|--------------|----------------|
| 1 | `checkout` | `actions/checkout@v4` | — | рабочая копия |
| 2 | `setup-java` | `actions/setup-java@v4` с `java-version: 17`, `distribution: temurin` | — | JDK 17 в PATH |
| 3 | `download-metrics-artifact` | `actions/download-artifact@v4` | artifact `metrics-json` | `metrics.json` в рабочей директории |
| 4 | `build-asset-generator` | `mvn compile -f asset-generator/pom.xml -q` | `asset-generator/pom.xml` | скомпилированные классы |
| 5 | `run-asset-generator` | `mvn exec:java -f asset-generator/pom.xml` | XML-спецификации из `game-content/` | PNG-файлы в `generated-assets/` |
| 6 | `scan-assets` | `bash scripts/scan-assets.sh` | `generated-assets/**/*.png`, `sprite-atlas.json` | `assets-manifest.json` |
| 7 | `upload-assets-artifact` | `actions/upload-artifact@v4` | `assets-manifest.json`, `generated-assets/` | artifact `assets-bundle` |

**Обработка ошибок шагов 4–5:** `continue-on-error: true`. При сбое — `assets-manifest.json` = `{"assets": [], "fallback": true}`. HTML-шаблон проверяет флаг `fallback: true` и показывает placeholder в Assets Gallery.

**Контракт выхода (`assets-manifest.json`):** см. раздел 4.2.

---

### 2.3. job: `build-html`

**Зависимость:** `needs: [collect-metrics, collect-assets]`

**Назначение:** Получить оба артефакта, инъецировать данные в HTML-шаблон, скопировать PNG, записать финальный `output/index.html`.

**Steps:**

| # | Имя шага | Действие | Входные файлы | Выходные данные |
|---|---------|---------|--------------|----------------|
| 1 | `checkout` | `actions/checkout@v4` | — | рабочая копия (для шаблона) |
| 2 | `download-metrics-artifact` | `actions/download-artifact@v4` | artifact `metrics-json` | `metrics.json` |
| 3 | `download-assets-artifact` | `actions/download-artifact@v4` | artifact `assets-bundle` | `assets-manifest.json`, `generated-assets/` |
| 4 | `inject-data-into-html` | `bash scripts/build-dashboard.sh` | `metrics.json`, `assets-manifest.json`, `frontend/templates/dashboard.html` | `output/index.html` |
| 5 | `copy-assets` | `cp -r generated-assets/ output/assets/` | `generated-assets/` | `output/assets/` |
| 6 | `copy-json` | копирование JSON-файлов в `output/` | `metrics.json`, `assets-manifest.json` | `output/metrics.json`, `output/assets-manifest.json` |
| 7 | `upload-output-artifact` | `actions/upload-artifact@v4` | `output/` | artifact `dashboard-output` |

**Контракт `build-dashboard.sh`:**
- Скрипт читает `metrics.json` и `assets-manifest.json`
- Заменяет placeholder-метки в шаблоне (формат: `{{METRICS_JSON_INLINE}}`, `{{ASSETS_MANIFEST_INLINE}}`)
- Инлайн-вставка JSON в `<script>` тег шаблона для client-side рендеринга
- Если `assets-manifest.json` содержит `"fallback": true` — оставить placeholder-секцию Assets Gallery нетронутой

---

### 2.4. job: `deploy`

**Зависимость:** `needs: build-html`

**Назначение:** Опубликовать директорию `output/` на GitHub Pages (ветка `gh-pages`).

**Permissions (job-level):**
```
permissions:
  contents: write
```

**Steps:**

| # | Имя шага | Действие | Входные файлы | Выходные данные |
|---|---------|---------|--------------|----------------|
| 1 | `checkout` | `actions/checkout@v4` | — | рабочая копия (для токена) |
| 2 | `download-output-artifact` | `actions/download-artifact@v4` | artifact `dashboard-output` | `output/` |
| 3 | `deploy-to-gh-pages` | `peaceiris/actions-gh-pages@v3` | `output/` | ветка `gh-pages` обновлена |

**Параметры `peaceiris/actions-gh-pages@v3`:**

| Параметр | Значение |
|---------|---------|
| `github_token` | `${{ secrets.GITHUB_TOKEN }}` |
| `publish_dir` | `./output` |
| `publish_branch` | `gh-pages` |
| `commit_message` | `ci: update dashboard [skip ci]` |
| `force_orphan` | `true` (чистая история в `gh-pages`) |

> **Обоснование выбора инструмента:** см. ADR-004 в разделе 7.

---

## 3. Спецификация bash-скриптов

> **Ссылка на SDD §7 (Task Board и декомпозиция):** Каждый скрипт — атомарный артефакт с чётким контрактом входа/выхода, чтобы Java Developer не додумывал скрытые требования.

Все скрипты размещаются в `scripts/` корня репозитория. Скрипты создаёт **Java Developer** по TASK-BE-022 и TASK-BE-023.

---

### 3.1. `scripts/count-tasks.sh`

**Назначение:** Подсчёт задач в Task Board по статусам.

**Входные файлы:** `tasks/**/*.md`

**Алгоритм:**
1. Рекурсивный обход всех `.md` файлов в директории `tasks/`
2. В каждом файле найти строки вида `| **Статус** | TODO |`, `| **Статус** | IN_PROGRESS |`, `| **Статус** | DONE |`
3. Паттерн поиска (регистронезависимый): `\|\s*\*\*Статус\*\*\s*\|\s*(TODO|IN_PROGRESS|DONE)\s*\|`
4. Подсчитать количество совпадений для каждого статуса
5. Если директория `tasks/` отсутствует или пуста — вернуть нули (не ошибку)
6. Экспортировать переменные окружения в `$GITHUB_ENV`: `TASKS_TODO`, `TASKS_IN_PROGRESS`, `TASKS_DONE`

**Выходной формат (в `$GITHUB_ENV`):**
```
TASKS_TODO=5
TASKS_IN_PROGRESS=3
TASKS_DONE=12
```

**Пример выходного JSON (для write-metrics.sh):**
```json
"taskBoard": {
  "todo": 5,
  "inProgress": 3,
  "done": 12,
  "total": 20
}
```

**Граничные случаи:**
- `tasks/` не существует → `TASKS_TODO=0`, `TASKS_IN_PROGRESS=0`, `TASKS_DONE=0`
- Файл без строки статуса → пропустить файл

---

### 3.2. `scripts/count-backend.sh`

**Назначение:** Подсчёт Java-классов по DDD-слоям в `backend/src/`.

**Входные файлы:** `backend/src/**/*.java`

**Алгоритм:**
1. Поиск файлов: `find backend/src/main -name "*.java"` и `find backend/src/test -name "*.java"`
2. Классификация по путям:
   - Путь содержит `/domain/model/` → считать как `aggregates`
   - Путь содержит `/application/service/` → считать как `services`
   - Путь содержит `/infrastructure/web/controller/` → считать как `controllers`
   - Путь начинается с `backend/src/test/` → считать как `tests`
   - Иначе → категория `other` (не экспортируется в метрики)
3. Экспортировать переменные в `$GITHUB_ENV`

**Выходной формат (в `$GITHUB_ENV`):**
```
BACKEND_AGGREGATES=8
BACKEND_SERVICES=12
BACKEND_CONTROLLERS=6
BACKEND_TESTS=25
```

**Пример выходного JSON:**
```json
"backend": {
  "aggregates": 8,
  "services": 12,
  "controllers": 6,
  "tests": 25,
  "totalClasses": 51
}
```

**Граничные случаи:**
- `backend/src/` не существует → все нули
- Java-файлы вне DDD-слоёв → игнорировать (не ломать скрипт)

---

### 3.3. `scripts/count-narrative.sh`

**Назначение:** Подсчёт XML-спецификаций нарратива в `game-content/`.

**Входные файлы:** `game-content/**/*.xml`

**Алгоритм:**
1. `find game-content/quest -name "*.xml" 2>/dev/null | wc -l` → `NARRATIVE_QUESTS`
2. `find game-content/conflicts -name "*.xml" 2>/dev/null | wc -l` → `NARRATIVE_CONFLICTS`
3. `find game-content/npc -name "*.xml" 2>/dev/null | wc -l` → `NARRATIVE_NPC`
4. `find game-content/world-events -name "*.xml" 2>/dev/null | wc -l` → `NARRATIVE_WORLD_EVENTS`
5. Проверить существование `game-content/manifest.xml` → `NARRATIVE_WITH_MANIFEST=true/false`
6. Если поддиректория отсутствует → нули для этой категории

**Выходной формат (в `$GITHUB_ENV`):**
```
NARRATIVE_QUESTS=4
NARRATIVE_CONFLICTS=2
NARRATIVE_NPC=6
NARRATIVE_WORLD_EVENTS=1
NARRATIVE_WITH_MANIFEST=true
```

**Пример выходного JSON:**
```json
"narrative": {
  "quests": 4,
  "conflicts": 2,
  "npc": 6,
  "worldEvents": 1,
  "hasManifest": true,
  "totalSpecs": 13
}
```

---

### 3.4. `scripts/fetch-ci-status.sh`

**Назначение:** Получить статус последнего CI-запуска из GitHub API.

**Входные данные:** `GITHUB_TOKEN`, `GITHUB_REPOSITORY`, `GITHUB_SHA`

**Алгоритм:**
1. HTTP GET: `https://api.github.com/repos/${GITHUB_REPOSITORY}/commits/${GITHUB_SHA}/check-runs`
2. Заголовки: `Authorization: Bearer $GITHUB_TOKEN`, `Accept: application/vnd.github+json`
3. Парсинг ответа с помощью `jq`:
   - Если все `check_runs[].conclusion == "success"` → `CI_STATUS=success`
   - Если хотя бы один `conclusion == "failure"` → `CI_STATUS=failure`
   - Если ни одного check-run → `CI_STATUS=no_runs`
4. При любой ошибке (network, HTTP 4xx/5xx, jq parse error) → `CI_STATUS=unknown`
5. `continue-on-error: true` на уровне шага в workflow

**Выходной формат (в `$GITHUB_ENV`):**
```
CI_STATUS=success
```

**Пример выходного JSON:**
```json
"ciStatus": "success"
```

---

### 3.5. `scripts/write-metrics.sh`

**Назначение:** Собрать все переменные из `$GITHUB_ENV` и записать итоговый `metrics.json`.

**Входные данные:** Переменные окружения от шагов 2–5 job `collect-metrics`, `GITHUB_SHA`, `GITHUB_REF_NAME`

**Алгоритм:**
1. Читать переменные окружения
2. Сформировать JSON-документ (см. раздел 4.1)
3. Записать в `metrics.json`

---

### 3.6. `scripts/scan-assets.sh`

**Назначение:** Обойти `generated-assets/` и сформировать `assets-manifest.json`.

**Входные файлы:** `generated-assets/**/*.png`, `generated-assets/**/sprite-atlas.json` (опционально)

**Алгоритм:**
1. Рекурсивный `find generated-assets -name "*.png"` → список PNG
2. Для каждого PNG:
   - Определить категорию по пути (`characters/`, `locations/`, `items/`, `ui/`)
   - Проверить наличие `sprite-atlas.json` в той же директории
   - Если атлас есть → прочитать `frames` из JSON
   - Если атласа нет → `frames: []`
3. Если `generated-assets/` пуста или не существует → `{"assets": [], "generatedAt": "...", "fallback": true}`
4. Записать `assets-manifest.json`

**Контракт выхода (`assets-manifest.json`):** см. раздел 4.2.

---

### 3.7. `scripts/build-dashboard.sh`

**Назначение:** Инъекция данных в HTML-шаблон, создание `output/index.html`.

**Входные файлы:** `metrics.json`, `assets-manifest.json`, `frontend/templates/dashboard.html`

**Алгоритм:**
1. Прочитать `metrics.json` и `assets-manifest.json`
2. Встроить содержимое как JS-переменные в шаблон (замена плейсхолдера `{{METRICS_JSON_INLINE}}`)
3. Встроить `assets-manifest.json` (замена `{{ASSETS_MANIFEST_INLINE}}`)
4. Если `assets-manifest.json` содержит `"fallback": true` — оставить placeholder-секцию Assets Gallery нетронутой
5. Записать результат в `output/index.html`

**Инструмент замены:** `sed` или `python3 -c` для inline-замены (решение оставлено Java Developer, см. TASK-BE-022).

---

## 4. Структура артефактов `output/`

> **Ссылка на SDD §6.4 (Форматы данных):** Каждый формат описан структурой, схемой полей и ссылками на смежные спецификации.

### 4.1. `output/metrics.json`

```json
{
  "generatedAt": "2026-04-02T06:00:00Z",
  "commitSha": "b284c9d...",
  "branch": "main",
  "ciStatus": "success",
  "taskBoard": {
    "todo": 5,
    "inProgress": 3,
    "done": 12,
    "total": 20
  },
  "backend": {
    "aggregates": 8,
    "services": 12,
    "controllers": 6,
    "tests": 25,
    "totalClasses": 51
  },
  "narrative": {
    "quests": 4,
    "conflicts": 2,
    "npc": 6,
    "worldEvents": 1,
    "hasManifest": true,
    "totalSpecs": 13
  }
}
```

**Поля:**

| Поле | Тип | Обязательное | Описание |
|------|-----|-------------|----------|
| `generatedAt` | ISO 8601 string | да | время генерации (UTC) |
| `commitSha` | string | да | SHA коммита, вызвавшего пайплайн |
| `branch` | string | да | ветка (`main`) |
| `ciStatus` | `"success"` \| `"failure"` \| `"unknown"` \| `"no_runs"` | да | статус CI последнего коммита |
| `taskBoard.todo` | integer ≥ 0 | да | количество задач в статусе TODO |
| `taskBoard.inProgress` | integer ≥ 0 | да | количество задач IN_PROGRESS |
| `taskBoard.done` | integer ≥ 0 | да | количество выполненных задач |
| `taskBoard.total` | integer ≥ 0 | да | сумма всех задач |
| `backend.totalClasses` | integer ≥ 0 | да | сумма aggregates+services+controllers |
| `narrative.totalSpecs` | integer ≥ 0 | да | сумма всех XML-файлов |
| `narrative.hasManifest` | boolean | да | наличие `manifest.xml` |

---

### 4.2. `output/assets-manifest.json`

```json
{
  "generatedAt": "2026-04-02T06:00:12Z",
  "fallback": false,
  "totalAssets": 14,
  "assets": [
    {
      "id": "tatyana-idle",
      "category": "characters",
      "path": "assets/characters/tatyana-idle.png",
      "hasAtlas": true,
      "frames": ["frame_0", "frame_1", "frame_2"]
    },
    {
      "id": "location-home",
      "category": "locations",
      "path": "assets/locations/location-home.png",
      "hasAtlas": false,
      "frames": []
    }
  ]
}
```

**Поля:**

| Поле | Тип | Описание |
|------|-----|---------|
| `generatedAt` | ISO 8601 string | время генерации |
| `fallback` | boolean | `true` если asset-generator не запустился |
| `totalAssets` | integer ≥ 0 | количество PNG-файлов |
| `assets[].id` | string | уникальный ID ассета (имя файла без расширения) |
| `assets[].category` | string | `characters` / `locations` / `items` / `ui` |
| `assets[].path` | string | относительный путь от корня `output/` |
| `assets[].hasAtlas` | boolean | наличие `sprite-atlas.json` |
| `assets[].frames` | string[] | имена фреймов из атласа (или `[]`) |

---

### 4.3. Итоговая структура директории `output/`

```
output/
├── index.html                — главная страница Dashboard (Overview + Assets Gallery)
├── metrics.json              — данные метрик проекта
├── assets-manifest.json      — манифест сгенерированных ассетов
└── assets/                   — PNG-файлы ассетов
    ├── characters/
    │   ├── tatyana-idle.png
    │   └── sprite-atlas.json
    ├── locations/
    │   └── location-home.png
    ├── items/
    └── ui/
```

---

## 5. Конфигурация GitHub Pages

> **Ссылка на SDD §6 (Нефункциональные требования):** конфигурация должна быть воспроизводима и задокументирована.

### 5.1. Настройка репозитория

1. Перейти в `https://github.com/Sagat9881/life_of_t/settings/pages`
2. **Source:** `Deploy from a branch`
3. **Branch:** `gh-pages` / `/ (root)`
4. Сохранить → GitHub Pages активированы

### 5.2. Permissions для workflow

В файле workflow (создаётся Java Developer по TASK-BE-022) необходимо добавить на уровне job `deploy`:

```yaml
permissions:
  contents: write
```

> `peaceiris/actions-gh-pages@v3` требует `contents: write` для записи в ветку `gh-pages`. Если репозиторий использует `GITHUB_TOKEN` с ограниченными правами по умолчанию, необходимо также включить **Settings → Actions → Workflow permissions → Read and write permissions**.

### 5.3. Итоговый URL

После успешного первого запуска пайплайна Dashboard доступен по:  
`https://sagat9881.github.io/life_of_t/`

### 5.4. Проверка работоспособности

- После первого пуша в `main` — зайти во вкладку **Actions** и убедиться, что все 4 job завершились зелёным
- Убедиться, что ветка `gh-pages` создана в репозитории
- Открыть URL Dashboard и проверить отображение метрик и ассетов

---

## 6. Стратегия обработки ошибок

> **Ссылка на SDD §6 (Нефункциональные требования — надёжность):** система должна деградировать gracefully, не прерывая публикацию при частичных сбоях.

### 6.1. Матрица ошибок и fallback-значений

| Сбой | Шаг | Поведение | Fallback-значение | Флаг в JSON |
|------|-----|-----------|-------------------|-------------|
| `tasks/` отсутствует или пуста | `count-tasks` | продолжить | `todo: 0, inProgress: 0, done: 0` | — |
| `backend/src/` отсутствует | `count-backend` | продолжить | все поля = 0 | — |
| `game-content/` отсутствует | `count-narrative` | продолжить | все поля = 0, `hasManifest: false` | — |
| GitHub API недоступен / HTTP ошибка | `fetch-ci-status` | `continue-on-error: true` | `"ciStatus": "unknown"` | — |
| `asset-generator` не компилируется | `build-asset-generator` | `continue-on-error: true` | пропустить шаги 5–6 | `"fallback": true` в `assets-manifest.json` |
| `asset-generator` завершается с ошибкой | `run-asset-generator` | `continue-on-error: true` | `generated-assets/` пуста | `"fallback": true` |
| `generated-assets/` пуста | `scan-assets` | продолжить | `{"assets": [], "fallback": true}` | `"fallback": true` |
| `assets-manifest.json` содержит `fallback: true` | `inject-data-into-html` | оставить placeholder | Assets Gallery показывает заглушку | — |
| `output/` пуста | `deploy` | **завершить с ошибкой** (критический сбой) | — | — |

### 6.2. Правила применения `continue-on-error`

```
continue-on-error: true  → ТОЛЬКО опциональные шаги (asset-generator, GitHub API)
continue-on-error: false → ВСЕ обязательные шаги (checkout, write-json, deploy)
```

**Опциональные шаги** (graceful degradation):
- `fetch-ci-status`
- `build-asset-generator`
- `run-asset-generator`
- `scan-assets` (если `generated-assets/` не существует)

**Обязательные шаги** (failure останавливает пайплайн):
- `checkout`
- `write-metrics-json`
- `inject-data-into-html`
- `deploy-to-gh-pages`

### 6.3. Отображение ошибок на Dashboard

| Ситуация | Что видит пользователь |
|----------|----------------------|
| `ciStatus: "unknown"` | CI/CD секция: «Статус неизвестен (API недоступен)» |
| `taskBoard.total == 0` | Task Board: «Задачи не найдены» (без ошибки) |
| `assets-manifest.fallback == true` | Assets Gallery: placeholder «Ассеты генерируются...» |
| Все данные нулевые | Dashboard отображается с нулевыми значениями (не 500 ошибка) |

---

## 7. ADR

> Полный текст ADR-004 см. в `docs/decisions/ADR-004-pipeline-publish-tool.md`.

### ADR-004: Выбор инструмента публикации GitHub Pages

**Статус:** Принято  
**Дата:** 2026-04-02

**Проблема:** Для публикации `output/` на GitHub Pages доступны два основных варианта:
1. `peaceiris/actions-gh-pages@v3` — публикует в ветку `gh-pages`
2. `actions/deploy-pages@v4` + `actions/configure-pages@v4` + `actions/upload-pages-artifact@v3` — официальный стек GitHub Pages deployment

**Сравнение:**

| Критерий | `peaceiris/actions-gh-pages@v3` | `actions/deploy-pages@v4` |
|---------|--------------------------------|--------------------------|
| Сложность настройки | низкая (1 шаг) | средняя (3 шага + configure) |
| Требования к permissions | `contents: write` | `pages: write`, `id-token: write` |
| Поддержка произвольной ветки | да (`gh-pages`) | нет (только `github-pages` environment) |
| Совместимость с простыми репозиториями | высокая | требует включить Pages в Settings заранее |
| Стабильность | v3 — стабильный, >10k stars | v4 — официальный, но требует OIDC-настройки |
| Флаг `force_orphan` (чистая история) | поддерживается | нет аналога |

**Решение:** Использовать `peaceiris/actions-gh-pages@v3`.

**Обоснование:**
- Минимальная конфигурация — один шаг против трёх
- Не требует OIDC-токенов и специальных environment-настроек в репозитории
- Флаг `force_orphan: true` обеспечивает чистую историю ветки `gh-pages` (не раздувает репозиторий)
- Широкое сообщество и проверенная стабильность

**Последствия:**
- Java Developer использует `peaceiris/actions-gh-pages@v3` при написании YAML (TASK-BE-022)
- В Settings репозитория: Pages source = `gh-pages` branch, `/` (root)
- При миграции на OIDC-based deployment в будущем — переход на `actions/deploy-pages@v4` без изменения скриптов

---

## Критерии готовности (DoD)

> **Ссылка на SDD §10 (Чеклист системного аналитика):** перед завершением фазы аналитик проверяет полноту спецификации.

- [x] `docs/specs/PIPELINE_SPEC.md` создан и покрывает все 4 jobs
- [x] Bash-скрипты специфицированы: алгоритм + контракт входа/выхода + пример JSON
- [x] ADR о выборе инструмента публикации задокументирован (раздел 7 + `docs/decisions/ADR-004`)
- [x] Задачи TASK-BE-022, TASK-BE-023, TASK-FE-054, TASK-FE-055 созданы
- [x] Структура `output/` задокументирована (раздел 4.3)
- [x] Способ настройки GitHub Pages описан (раздел 5.1)
- [x] Стратегия обработки ошибок и fallback-значения задокументированы (раздел 6)
- [x] Пайплайн не требует платных credits (только `ubuntu-latest`)
