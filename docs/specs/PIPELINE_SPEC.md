# PIPELINE_SPEC — Спецификация GitHub Actions Pipeline для Project Status Dashboard

**ID:** SPEC-SA-013  
**Версия:** 1.0  
**Дата:** 2026-03-31  
**Зависимости:** SPEC-SA-010, SPEC-SA-011, SPEC-SA-012  
**Связанные задачи:** TASK-BE-022, TASK-BE-023, TASK-FE-054, TASK-FE-055  
**ADR:** `docs/decisions/ADR-003-pipeline-publish-tool.md`  
**Автор:** System Analyst  
**Методология:** SDD (Specify → Plan → Task → Implement) — см. `system-analyst-skill.md`, раздел 5

---

## 1. Архитектура пайплайна

### 1.1. Диаграмма jobs и зависимостей

```
┌─────────────────────────────────────────────────────────────────┐
│  workflow: build-status-dashboard                               │
│  Триггеры: push[main] | workflow_dispatch | schedule(06:00 UTC) │
└─────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────┐
│  job: collect-metrics│
│  runner: ubuntu-latest│
│  outputs:            │
│    metrics-artifact  │
└─────────┬───────────┘
          │ needs: collect-metrics
          ▼
┌─────────────────────┐
│  job: collect-assets │
│  runner: ubuntu-latest│
│  continue-on-error: false│
│  outputs:            │
│    assets-artifact   │
└─────────┬───────────┘
          │ needs: [collect-metrics, collect-assets]
          ▼
┌─────────────────────┐
│  job: build-html     │
│  runner: ubuntu-latest│
│  outputs:            │
│    output-dir        │
└─────────┬───────────┘
          │ needs: build-html
          ▼
┌─────────────────────┐
│  job: deploy         │
│  runner: ubuntu-latest│
│  permissions:        │
│    pages: write      │
│    id-token: write   │
└─────────────────────┘
```

### 1.2. Передача данных между jobs

Каждый job сохраняет выходные файлы через `actions/upload-artifact@v4`; следующий job загружает через `actions/download-artifact@v4`.

| Артефакт              | Создаётся в job   | Потребляется в job       |
|-----------------------|-------------------|--------------------------|
| `metrics-artifact`    | collect-metrics   | build-html               |
| `assets-artifact`     | collect-assets    | build-html               |
| `output-artifact`     | build-html        | deploy                   |

---

## 2. Спецификация jobs

### 2.1. Job `collect-metrics`

**Назначение:** собрать числовые метрики из репозитория и записать `metrics.json`.  
**Runner:** `ubuntu-latest`  
**Permissions:** `contents: read`

#### Steps

| # | Step ID | Действие | Входные данные | Выходные данные |
|---|---------|----------|----------------|-----------------|
| 1 | `checkout` | `actions/checkout@v4` | — | рабочая копия репозитория |
| 2 | `count-tasks` | `bash scripts/ci/count-tasks.sh` | `tasks/**/*.md` | переменные окружения `TASKS_JSON` |
| 3 | `count-backend` | `bash scripts/ci/count-backend.sh` | `backend/src/**/*.java` | переменная `BACKEND_JSON` |
| 4 | `count-narrative` | `bash scripts/ci/count-narrative.sh` | `game-content/**/*.xml` | переменная `NARRATIVE_JSON` |
| 5 | `fetch-ci-status` | `bash scripts/ci/fetch-ci-status.sh` | `GITHUB_TOKEN` (env), `GITHUB_REPOSITORY` (env) | переменная `CI_JSON`; `continue-on-error: true` |
| 6 | `write-metrics` | `bash scripts/ci/write-metrics.sh` | `TASKS_JSON`, `BACKEND_JSON`, `NARRATIVE_JSON`, `CI_JSON` | файл `artifacts/metrics.json` |
| 7 | `upload-metrics` | `actions/upload-artifact@v4` | `artifacts/metrics.json` | артефакт `metrics-artifact` |

**Environment variables:**

| Переменная | Источник | Описание |
|------------|----------|----------|
| `GITHUB_TOKEN` | GitHub Actions (автоматически) | Для запроса статуса CI через API |
| `GITHUB_REPOSITORY` | GitHub Actions (автоматически) | `owner/repo` для API-запросов |

---

### 2.2. Job `collect-assets`

**Назначение:** собрать или проверить сгенерированные ассеты и записать `assets-manifest.json`.  
**Runner:** `ubuntu-latest`  
**Permissions:** `contents: read`  
**needs:** `collect-metrics`

> **Важно:** Job НЕ запускает `asset-generator` при каждом CI-прогоне (это дорого и долго).
> Вместо этого сканирует директорию `generated-assets/` если она присутствует в репозитории.
> Если `asset-generator` нужен — это отдельный workflow, запускаемый вручную.

#### Steps

| # | Step ID | Действие | Входные данные | Выходные данные |
|---|---------|----------|----------------|-----------------|
| 1 | `checkout` | `actions/checkout@v4` | — | рабочая копия |
| 2 | `check-assets-dir` | `bash`: проверить наличие `generated-assets/` | файловая система | переменная `ASSETS_EXIST` (true/false) |
| 3 | `scan-assets` | `bash scripts/ci/scan-assets.sh` | `generated-assets/` | файл `artifacts/assets-manifest.json`; `continue-on-error: true` |
| 4 | `fallback-manifest` | `bash`: если `scan-assets` упал — создать пустой манифест | — | файл `artifacts/assets-manifest.json` (fallback) |
| 5 | `upload-assets` | `actions/upload-artifact@v4` | `artifacts/assets-manifest.json` | артефакт `assets-artifact` |

**Fallback-манифест** (при отсутствии `generated-assets/`):
```json
{
  "generatedAt": "<ISO-8601 текущего времени>",
  "categories": {
    "npc": [], "locations": [], "furniture": [], "pets": [], "ui": []
  }
}
```

---

### 2.3. Job `build-html`

**Назначение:** объединить метрики и ассеты в итоговую HTML-страницу.  
**Runner:** `ubuntu-latest`  
**Permissions:** `contents: read`  
**needs:** `[collect-metrics, collect-assets]`

#### Steps

| # | Step ID | Действие | Входные данные | Выходные данные |
|---|---------|----------|----------------|-----------------|
| 1 | `checkout` | `actions/checkout@v4` | — | рабочая копия |
| 2 | `download-metrics` | `actions/download-artifact@v4` | артефакт `metrics-artifact` | `artifacts/metrics.json` |
| 3 | `download-assets` | `actions/download-artifact@v4` | артефакт `assets-artifact` | `artifacts/assets-manifest.json` |
| 4 | `inject-data` | JS/bash: вставить JSON в HTML-шаблон | `frontend/status/index.template.html`, оба JSON | `output/index.html` |
| 5 | `copy-assets` | `bash`: `cp -r generated-assets/ output/assets/` | `generated-assets/` (если есть) | `output/assets/` |
| 6 | `copy-manifests` | `bash`: скопировать JSON-файлы | `artifacts/*.json` | `output/metrics.json`, `output/assets-manifest.json` |
| 7 | `upload-output` | `actions/upload-artifact@v4` | директория `output/` | артефакт `output-artifact` |

**Контракт инъекции данных (step `inject-data`):**
- Скрипт читает `index.template.html`.
- Заменяет плейсхолдер `<!-- METRICS_JSON_PLACEHOLDER -->` на `<script>window.__METRICS__ = {...}</script>`.
- Заменяет плейсхолдер `<!-- ASSETS_MANIFEST_PLACEHOLDER -->` на `<script>window.__ASSETS_MANIFEST__ = {...}</script>`.
- Сохраняет результат в `output/index.html`.

---

### 2.4. Job `deploy`

**Назначение:** опубликовать директорию `output/` на GitHub Pages.  
**Runner:** `ubuntu-latest`  
**Permissions:** `pages: write`, `id-token: write`  
**needs:** `build-html`  
**environment:** `github-pages`

#### Steps

| # | Step ID | Действие | Входные данные | Выходные данные |
|---|---------|----------|----------------|-----------------|
| 1 | `download-output` | `actions/download-artifact@v4` | артефакт `output-artifact` | директория `output/` |
| 2 | `setup-pages` | `actions/configure-pages@v5` | — | конфигурация Pages |
| 3 | `upload-pages` | `actions/upload-pages-artifact@v3` | `path: output/` | Pages-артефакт |
| 4 | `deploy-pages` | `actions/deploy-pages@v4` | Pages-артефакт | URL `https://sagat9881.github.io/life_of_t/` |

> Решение об инструменте публикации: см. `docs/decisions/ADR-003-pipeline-publish-tool.md`.

---

## 3. Спецификация bash-скриптов

Все скрипты располагаются в `scripts/ci/`. Права: `chmod +x`. Shell: `bash` (bash 5+, ubuntu-latest).

### 3.1. `count-tasks.sh`

**Входные данные:** `tasks/**/*.md`  
**Выходные данные:** переменная окружения `TASKS_JSON` (JSON-строка)

**Алгоритм:**
1. Найти все файлы `find tasks/ -name '*.md' -type f`.
2. Для каждого файла найти строку, соответствующую паттерну `**Статус:**` или `| **Статус** |`.
3. Извлечь значение статуса: `TODO`, `IN_PROGRESS`, `DONE`.
4. Подсчитать количество файлов в каждой группе.
5. Записать в `$GITHUB_ENV`: `TASKS_JSON={"todo": N, "inProgress": N, "done": N, "total": N}`.

**Пример выходного значения:**
```json
{"todo": 5, "inProgress": 2, "done": 8, "total": 15}
```

**Граничный случай:** если `tasks/` не существует или пуста → все значения `0`, скрипт завершается с кодом `0`.

---

### 3.2. `count-backend.sh`

**Входные данные:** `backend/src/**/*.java`  
**Выходные данные:** переменная `BACKEND_JSON`

**Алгоритм:**
1. `find backend/src -name '*.java' -path '*/domain/model/*'` → `aggregates`
2. `find backend/src -name '*.java' -path '*/application/service/*'` → `services`
3. `find backend/src -name '*.java' -path '*/infrastructure/web/controller/*'` → `controllers`
4. `find backend/src -name '*.java' -path '*/test/*'` → `tests`
5. Все Java-файлы → `total`
6. Записать в `$GITHUB_ENV`: `BACKEND_JSON={...}`.

**Пример выходного значения:**
```json
{
  "aggregates": 4,
  "services": 7,
  "controllers": 3,
  "tests": 12,
  "total": 31
}
```

**Граничный случай:** если `backend/src` не существует → все `0`, код `0`.

---

### 3.3. `count-narrative.sh`

**Входные данные:** `game-content/**/*.xml`  
**Выходные данные:** переменная `NARRATIVE_JSON`

**Алгоритм:**
1. `find game-content/quest -name '*.xml'` → `quests`
2. `find game-content/conflicts -name '*.xml'` → `conflicts`
3. `find game-content/npc -name '*.xml'` → `npc`
4. `find game-content/world-events -name '*.xml'` → `worldEvents`
5. Проверить наличие `game-content/manifest.xml` → `withManifest` (true/false)
6. Записать в `$GITHUB_ENV`.

**Пример выходного значения:**
```json
{
  "quests": 3,
  "conflicts": 5,
  "npc": 8,
  "worldEvents": 2,
  "withManifest": true
}
```

**Граничный случай:** отдельные поддиректории могут отсутствовать → соответствующее поле `0`.

---

### 3.4. `fetch-ci-status.sh`

**Входные данные:** `GITHUB_TOKEN` (env), `GITHUB_REPOSITORY` (env)  
**Выходные данные:** переменная `CI_JSON`  
**continue-on-error: true**

**Алгоритм:**
1. Запросить GitHub API: `GET /repos/{owner}/{repo}/commits/main/check-runs` с заголовком `Authorization: Bearer $GITHUB_TOKEN`.
2. Из ответа извлечь поля `total_count`, `check_runs[*].conclusion`.
3. Подсчитать `success` / `failure` / `in_progress`.
4. Записать в `$GITHUB_ENV`.

**Пример выходного значения:**
```json
{
  "lastBuildStatus": "success",
  "passedChecks": 3,
  "failedChecks": 0
}
```

**Граничный случай:** API недоступен или `curl` вернул ненулевой код → `CI_JSON={"lastBuildStatus": "unknown", "passedChecks": 0, "failedChecks": 0}`; скрипт завершается кодом `0`.

---

### 3.5. `write-metrics.sh`

**Входные данные:** переменные `TASKS_JSON`, `BACKEND_JSON`, `NARRATIVE_JSON`, `CI_JSON`, `GITHUB_SHA`, `GITHUB_RUN_NUMBER`  
**Выходные данные:** файл `artifacts/metrics.json`

**Алгоритм:**
1. Создать директорию `artifacts/` если не существует.
2. Объединить все JSON-переменные в итоговый объект согласно схеме из `docs/specs/STATUS_PAGE_OVERVIEW_SPEC.md`.
3. Добавить `generatedAt` (ISO-8601) и `buildNumber` (`GITHUB_RUN_NUMBER`).
4. Записать файл `artifacts/metrics.json`.

**Пример выходного значения:** см. `docs/specs/STATUS_PAGE_OVERVIEW_SPEC.md`, раздел «Схема metrics.json».

---

### 3.6. `scan-assets.sh`

**Входные данные:** директория `generated-assets/`  
**Выходные данные:** файл `artifacts/assets-manifest.json`  
**continue-on-error: true**

**Алгоритм:**
1. Проверить наличие `generated-assets/`; если нет → записать fallback-манифест (раздел 2.2) и завершиться с кодом `0`.
2. Для каждой категории (`characters`→`npc`, `locations`, `furniture`, `pets`, `ui`):
   a. Найти все поддиректории в `generated-assets/{category}/`.
   b. Для каждой поддиректории найти PNG (`find . -maxdepth 1 -name '*.png'`).
   c. Если есть `sprite-atlas.json` — прочитать поля `animations`.
   d. Сформировать объект `assetEntry` по схеме из `docs/specs/STATUS_PAGE_ASSETS_SPEC.md`, раздел 2.1.
3. Собрать итоговый JSON; добавить `generatedAt`.
4. Записать `artifacts/assets-manifest.json`.

**Граничный случай:** PNG найден, но `sprite-atlas.json` отсутствует → `metaPath: null`, `animations: []`.

---

## 4. Структура артефактов

### 4.1. Директория `output/` (публикуется на GitHub Pages)

```
output/
├── index.html              — итоговая страница Dashboard
├── metrics.json            — данные метрик (для отладки)
├── assets-manifest.json    — манифест ассетов (для отладки)
└── assets/
    ├── characters/
    │   └── tanya/
    │       ├── idle_atlas.png
    │       └── sprite-atlas.json
    ├── locations/
    ├── furniture/
    ├── pets/
    └── ui/
        └── placeholder.png  — ОБЯЗАТЕЛЕН
```

### 4.2. Промежуточные артефакты GitHub Actions

| Имя артефакта     | Содержимое                                 | TTL (retention) |
|-------------------|--------------------------------------------|------------------|
| `metrics-artifact`| `metrics.json`                             | 7 дней           |
| `assets-artifact` | `assets-manifest.json`                     | 7 дней           |
| `output-artifact` | полная директория `output/` (без `assets/`)| 1 день           |

> `assets/` в `output-artifact` не включаются — их размер может быть значительным. Ассеты копируются напрямую в job `build-html` из checkout.

### 4.3. Итоговый URL

```
https://sagat9881.github.io/life_of_t/
```

Путь настраивается через Settings → Pages → Source (см. раздел 5).

---

## 5. Конфигурация GitHub Pages

### 5.1. Настройка в репозитории

1. Перейти в `Settings → Pages` репозитория `Sagat9881/life_of_t`.
2. В разделе **Source** выбрать: **GitHub Actions** (не ветка).
3. Сохранить.

> При выборе Source = «GitHub Actions» публикацию управляет workflow через `actions/deploy-pages@v4`. Ветка `gh-pages` **не нужна**.

### 5.2. Permissions в workflow

Файл `.github/workflows/build-dashboard.yml` (создаёт Java Developer по TASK-BE-022) обязан содержать:

```yaml
permissions:
  contents: read
  pages: write
  id-token: write
```

> `pages: write` и `id-token: write` обязательны для `actions/deploy-pages@v4`.

### 5.3. Среда (Environment)

Job `deploy` обязан указывать:
```yaml
environment:
  name: github-pages
  url: ${{ steps.deploy.outputs.page_url }}
```

Среда `github-pages` создаётся автоматически при первом деплое через официальные Pages-экшены.

### 5.4. Ограничения

- Максимальный размер публикуемого сайта: **1 GB** (GitHub Pages limit).
- Частота публикации: не более **10 деплоев в час** на репозиторий.
- Workflow использует только `ubuntu-latest` — бесплатные минуты GitHub Actions.

---

## 6. Стратегия обработки ошибок и fallback-значения

| Ситуация | Поведение | Fallback-значение |
|----------|-----------|-------------------|
| `tasks/` пуста или отсутствует | Скрипт завершается с `exit 0` | `{"todo":0,"inProgress":0,"done":0,"total":0}` |
| `backend/src/` отсутствует | Скрипт завершается с `exit 0` | Все поля `0` |
| `game-content/` отсутствует | Скрипт завершается с `exit 0` | Все поля `0`, `withManifest: false` |
| GitHub API недоступен | `continue-on-error: true` на step `fetch-ci-status` | `{"lastBuildStatus":"unknown","passedChecks":0,"failedChecks":0}` |
| `generated-assets/` отсутствует | `scan-assets.sh` пишет пустой манифест | Все категории `[]` |
| `sprite-atlas.json` отсутствует у ассета | `scan-assets.sh` продолжает | `metaPath: null`, `animations: []` |
| Job `collect-assets` завершился с ошибкой | Job `build-html` использует fallback-манифест | Assets Gallery показывает placeholder |
| `index.template.html` не найден | Job `build-html` завершается с **ошибкой** (блокирующий) | Нет fallback — критическая ошибка |
| Деплой на GitHub Pages завершился ошибкой | Workflow **падает** с ошибкой; предыдущая версия остаётся доступной | — |

### 6.1. Уровни критичности

- **Блокирующие** (failure останавливает весь workflow): отсутствие `index.template.html`, ошибка деплоя.
- **Некритичные** (`continue-on-error: true`): GitHub API, scan-assets, сборка asset-generator.
- **Молчаливые** (скрипт возвращает `0` с fallback): пустые директории метрик.

---

## 7. ADR — Выбор инструмента публикации

См. полный текст: `docs/decisions/ADR-003-pipeline-publish-tool.md`

**Кратко:** Принято решение использовать официальный стек `actions/configure-pages` + `actions/upload-pages-artifact` + `actions/deploy-pages@v4` вместо `peaceiris/actions-gh-pages`, поскольку официальный стек поддерживает `environment: github-pages` и OIDC-токены без PAT.

---

## 8. Метрики и критерии готовности

> Согласно `system-analyst-skill.md`, раздел 6 (п.6) и раздел 9.

| Критерий | Проверяет |
|----------|----------|
| Workflow завершается успешно на `push[main]` | TASK-BE-022 |
| `metrics.json` содержит все поля схемы (SPEC-SA-011) | TASK-BE-022 |
| `assets-manifest.json` валиден по JSON Schema (SPEC-SA-012) | TASK-BE-023 |
| `index.html` доступен по `https://sagat9881.github.io/life_of_t/` | TASK-FE-055 |
| При пустом `generated-assets/` страница не падает | TASK-BE-023 + TASK-FE-054 |
| При недоступном GitHub API workflow не падает | TASK-BE-022 |
| Время выполнения полного workflow ≤ 10 минут | все |

---

*Спецификация создана согласно SDD-фазе **Specify** (`system-analyst-skill.md`, раздел 5).*  
*Следующий шаг: Java Developer принимает TASK-BE-022 и TASK-BE-023; JS Developer — TASK-FE-054 и TASK-FE-055.*
