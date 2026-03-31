# PROJECT_METRICS_SPEC.md

> **Версия:** 1.0  
> **Автор:** System Analyst  
> **Дата:** 2026-03-31  
> **Эпик:** EPIC-DEVOPS-001 — Project Status Dashboard  
> **Фаза:** 1 — Спецификация метрик  
> **Связанные задачи:** TASK-SA-010, TASK-BE-018, TASK-BE-019, TASK-BE-020  
> **Ссылка на skill:** `system-analyst-skill.md` §8 — Система метрик проекта

---

## Раздел 1. Глоссарий метрик

| Термин | Определение |
|--------|-------------|
| **Метрика верхнего уровня** | Агрегированный показатель, отражающий состояние одного из измерений проекта (Task Board, Backend, Assets, Narrative, CI/CD). Отображается как единственная карточка на странице статуса. |
| **Метрика нижнего уровня** | Атомарный числовой или булев показатель, из которого вычисляется метрика верхнего уровня. |
| **Источник данных** | Файл, директория или API-эндпоинт, из которого скрипт извлекает значение метрики. |
| **Формула расчёта** | Алгоритм (псевдокод или математическое выражение), по которому значение вычисляется из сырых данных источника. |
| **Формат отображения** | Способ визуализации метрики на HTML-странице: число, процент, прогресс-бар, статус-бейдж. |
| **metrics.json** | Файл-артефакт, генерируемый GitHub Actions пайплайном; является единственным источником данных для HTML-страницы статуса. |
| **Статус задачи** | Значение поля `Статус` в markdown-файле задачи. Допустимые значения: `TODO`, `IN_PROGRESS`, `DONE`, `BLOCKED`. |
| **Тип задачи** | Значение поля `Тип` в markdown-файле задачи. Допустимые значения: `backend`, `frontend`, `assets`, `narrative`, `analytics`. |
| **Агрегат (DDD)** | Java-класс, аннотированный как корень агрегата в слое `domain/model/`. |
| **Спринт-блок** | Директория в `game-content/`, соответствующая одному нарративному блоку; содержит `manifest.xml`. |
| **Атлас** | PNG-файл спрайт-атласа в `asset-generator/target/generated-assets/`; каждый атлас имеет парный `sprite-atlas.json`. |

---

## Раздел 2. Таблица метрик

### 2.1 Task Board Health (TBH)

> Отражает общий прогресс задач по всем ролям. Источник: §8.1 п.3 «Процессные метрики» skill-файла.

| ID | Название | Уровень | Источник данных | Формула | Формат отображения |
|----|----------|---------|----------------|---------|--------------------|
| TBH-01 | Всего задач | lower | `tasks/**/*.md` — все `.md`-файлы рекурсивно | `COUNT(all .md files in tasks/)` | Число |
| TBH-02 | % выполнения (DONE) | lower | `tasks/**/*.md`, поле `Статус: DONE` | `COUNT(Статус=DONE) / COUNT(all) * 100` | Прогресс-бар + % |
| TBH-03 | % выполнения по роли | lower | `tasks/**/*.md`, поля `Статус` и `Тип` | `COUNT(Тип=X AND Статус=DONE) / COUNT(Тип=X) * 100` для каждого `X ∈ {backend, frontend, assets, narrative}` | 4 мини-прогресс-бара |
| TBH-04 | Готовы к старту | lower | `tasks/**/*.md`, поля `Статус: TODO` и `Зависит от` | `COUNT(Статус=TODO AND (Зависит от=null OR все зависимости имеют Статус=DONE))` | Число |
| TBH-05 | Заблокированных задач | lower | `tasks/**/*.md`, поля `Статус: BLOCKED` или `Зависит от` → незакрытая задача | `COUNT(Статус=BLOCKED OR (Зависит от=X AND X.Статус≠DONE))` | Число + статус-бейдж (🔴/🟢) |

**Агрегированный показатель TBH:** `TBH-02` (% выполнения) — главное значение карточки.

---

### 2.2 Backend Completeness (BC)

> Отражает зрелость Java-бэкенда. Источник: §8.1 п.2 «Технические метрики» skill-файла, связь с DDD-слоями из `java-developer-skill.md`.

| ID | Название | Уровень | Источник данных | Формула | Формат отображения |
|----|----------|---------|----------------|---------|--------------------|
| BC-01 | Domain-агрегаты | lower | `backend/src/main/java/ru/lifegame/backend/domain/model/*/` | `COUNT(*.java files in domain/model/**/)` | Число |
| BC-02 | Application-сервисы | lower | `backend/src/main/java/ru/lifegame/backend/application/service/*.java` | `COUNT(*.java files)` | Число |
| BC-03 | REST-контроллеры | lower | `backend/src/main/java/ru/lifegame/backend/infrastructure/web/controller/*.java` | `COUNT(*.java files)` | Число |
| BC-04 | Покрытие unit-тестами | lower | `backend/src/test/` vs `backend/src/main/` | `COUNT(*.java in test/) / COUNT(*.java in main/) * 100` (приближение) | Прогресс-бар + % |
| BC-05 | Нарративные спеки без хардкода | lower | `backend/src/main/` — отсутствие строк типа `"hardcoded"` в XML-загрузчиках; связь TASK-BE-015/016 | `COUNT(xml-loader классов, не содержащих hardcoded строк квест-данных)` | Число |

**Агрегированный показатель BC:** среднее `(BC-01 + BC-02 + BC-03) / TARGET_SUM * 100` где `TARGET_SUM` = плановые значения из `docs/decisions/`.

---

### 2.3 Assets Coverage (AC)

> Отражает наполненность визуальных ассетов. Источник: §4.1 артефакты `tasks/assets/` и связь с генератором ассетов.

| ID | Название | Уровень | Источник данных | Формула | Формат отображения |
|----|----------|---------|----------------|---------|--------------------|
| AC-01 | Всего атласов (PNG) | lower | `asset-generator/target/generated-assets/**/*.png` | `COUNT(*.png files)` | Число |
| AC-02 | Атласы с метаданными | lower | `asset-generator/target/generated-assets/**/*.json` (спрайт-атласы) | `COUNT(sprite-atlas.json files)` | Число + % от AC-01 |
| AC-03 | Анимации по категориям | lower | `generated-assets/npc/`, `locations/`, `furniture/`, `pets/`, `ui/` | `COUNT(*.png per subdirectory)` для каждой категории | 5 чисел в таблице |
| AC-04 | Локации с полным набором | lower | `generated-assets/locations/{name}/` содержит `idle.png` + минимум 1 `variant_*.png` | `COUNT(dirs where idle.png AND variant_*.png exist)` | Число |
| AC-05 | NPC с полным набором | lower | `generated-assets/npc/{name}/` содержит все анимации из `sprite-atlas.json` | `COUNT(npc dirs where all animations in manifest exist)` | Число |

**Агрегированный показатель AC:** `AC-02 / AC-01 * 100` — % атласов с метаданными.

---

### 2.4 Narrative Coverage (NC)

> Отражает наполненность нарративного контента. Источник: §8 skill-файла, артефакты нарратора из `narrantor-skill.md`.

| ID | Название | Уровень | Источник данных | Формула | Формат отображения |
|----|----------|---------|----------------|---------|--------------------|
| NC-01 | Квесты | lower | `game-content/quest/*.xml` | `COUNT(*.xml files)` | Число |
| NC-02 | Конфликты | lower | `game-content/conflicts/*.xml` | `COUNT(*.xml files)` | Число |
| NC-03 | NPC-блоки | lower | `game-content/npc/*.xml` | `COUNT(*.xml files)` | Число |
| NC-04 | World Events | lower | `game-content/world-events/*.xml` | `COUNT(*.xml files)` | Число |
| NC-05 | Блоки с манифестом | lower | `game-content/**/manifest.xml` | `COUNT(manifest.xml files rекурсивно в game-content/)` | Число + % от суммы (NC-01+NC-02+NC-03+NC-04) |

**Агрегированный показатель NC:** `(NC-01 + NC-02 + NC-03 + NC-04)` — суммарное количество нарративных единиц.

---

### 2.5 CI/CD Health (CCH)

> Отражает состояние пайплайна и сборки. Источник: §8.1 п.2 «Технические метрики» skill-файла; данные через GitHub API.

| ID | Название | Уровень | Источник данных | Формула | Формат отображения |
|----|----------|---------|----------------|---------|--------------------|
| CCH-01 | Статус последнего run | lower | GitHub API: `GET /repos/Sagat9881/life_of_t/actions/runs?per_page=1` → поле `conclusion` | `runs[0].conclusion ∈ {success, failure, cancelled, skipped}` | Статус-бейдж (🟢/🔴/🟡) |
| CCH-02 | Время последней успешной сборки | lower | GitHub API: `GET /repos/.../actions/runs?status=success&per_page=1` → поле `updated_at` | `runs[0].updated_at` (ISO-8601) | Дата + время |
| CCH-03 | Наличие Dockerfile | lower | Файловая система репозитория: `Dockerfile` в корне | `EXISTS(Dockerfile) ? 1 : 0` | Статус-бейдж ✅/❌ |
| CCH-04 | Наличие docker-compose.yml | lower | `docker-compose.yml` в корне | `EXISTS(docker-compose.yml) ? 1 : 0` | Статус-бейдж ✅/❌ |
| CCH-05 | Доступность GitHub Pages | lower | GitHub API: `GET /repos/Sagat9881/life_of_t/pages` → поле `status` | `pages.status == 'built' ? OK : FAIL` | Статус-бейдж + URL |

**Агрегированный показатель CCH:** `CCH-01` — статус последнего run как основной индикатор.

---

## Раздел 3. Схема сбора данных

Каждая группа метрик собирается отдельным шагом GitHub Actions пайплайна. Скрипты создаются в рамках TASK-BE-018..020.

```
┌─────────────────────────────────────────────────────┐
│              GitHub Actions Pipeline                │
│                                                     │
│  step 1: collect-tasks      → TBH (TBH-01..05)     │
│    script: scripts/collect_tasks.py                 │
│    reads:  tasks/**/*.md                            │
│                                                     │
│  step 2: collect-backend    → BC (BC-01..05)        │
│    script: scripts/collect_backend.py               │
│    reads:  backend/src/main/**/*.java               │
│            backend/src/test/**/*.java               │
│                                                     │
│  step 3: collect-assets     → AC (AC-01..05)        │
│    script: scripts/collect_assets.py                │
│    reads:  asset-generator/target/generated-assets/ │
│                                                     │
│  step 4: collect-narrative  → NC (NC-01..05)        │
│    script: scripts/collect_narrative.py             │
│    reads:  game-content/**/*.xml                    │
│                                                     │
│  step 5: collect-cicd       → CCH (CCH-01..05)      │
│    script: scripts/collect_cicd.py                  │
│    calls:  GitHub API (GITHUB_TOKEN)                │
│                                                     │
│  step 6: merge-metrics      → metrics.json          │
│    script: scripts/merge_metrics.py                 │
│    input:  output of steps 1..5                     │
│    output: docs/status/metrics.json                 │
└─────────────────────────────────────────────────────┘
```

**Расположение скриптов:** `scripts/metrics/` в корне репозитория.  
**Расположение артефакта:** `docs/status/metrics.json` (публикуется через GitHub Pages).

---

## Раздел 4. JSON-схема metrics.json

Файл `metrics.json` — единственный источник данных для HTML-страницы статуса. Генерируется пайплайном при каждом пуше в `main`.

```json
{
  "$schema": "https://raw.githubusercontent.com/Sagat9881/life_of_t/main/docs/specs/metrics-schema.json",
  "generatedAt": "<ISO-8601 timestamp>",

  "taskBoard": {
    "total": 0,
    "done": 0,
    "inProgress": 0,
    "todo": 0,
    "blocked": 0,
    "readyToStart": 0,
    "completionPct": 0.0,
    "byRole": {
      "backend":   { "total": 0, "done": 0, "pct": 0.0 },
      "frontend":  { "total": 0, "done": 0, "pct": 0.0 },
      "assets":    { "total": 0, "done": 0, "pct": 0.0 },
      "narrative": { "total": 0, "done": 0, "pct": 0.0 }
    }
  },

  "backend": {
    "aggregates": 0,
    "services": 0,
    "controllers": 0,
    "testCoveragePct": 0.0,
    "narrativeSpecsWithoutHardcode": 0
  },

  "assets": {
    "totalAtlases": 0,
    "atlasesWithMetadata": 0,
    "metadataCoveragePct": 0.0,
    "byCategory": {
      "npc":       0,
      "locations": 0,
      "furniture": 0,
      "pets":      0,
      "ui":        0
    },
    "locationsComplete": 0,
    "npcComplete": 0
  },

  "narrative": {
    "quests": 0,
    "conflicts": 0,
    "npc": 0,
    "worldEvents": 0,
    "total": 0,
    "withManifest": 0,
    "manifestCoveragePct": 0.0
  },

  "cicd": {
    "lastRunStatus": "success | failure | cancelled | pending",
    "lastRunAt": "<ISO-8601 timestamp>",
    "lastSuccessAt": "<ISO-8601 timestamp>",
    "dockerfileExists": true,
    "dockerComposeExists": true,
    "pagesStatus": "built | not-built",
    "pagesUrl": "https://sagat9881.github.io/life_of_t/"
  }
}
```

### Правила валидации JSON:

1. Все числовые поля — неотрицательные целые или float (0.0–100.0 для `*Pct`).
2. `generatedAt` — обязательное поле, без него страница статуса показывает ошибку.
3. `lastRunStatus` — строго одно из: `success`, `failure`, `cancelled`, `pending`.
4. Если источник данных недоступен (директория отсутствует), скрипт записывает `null` и логирует предупреждение.
5. Файл не должен содержать секретов (токены, пароли) — только агрегированные показатели.

---

## Приложение A. Целевые значения метрик (v1.0)

| Группа | Метрика | Целевое значение | Критический порог |
|--------|---------|-----------------|-------------------|
| TBH | completionPct | ≥ 80% | < 20% |
| TBH | blocked | 0 | > 3 |
| BC | aggregates | ≥ 5 | 0 |
| BC | testCoveragePct | ≥ 60% | < 10% |
| AC | metadataCoveragePct | 100% | < 50% |
| NC | total | ≥ 10 | 0 |
| CCH | lastRunStatus | success | failure |

> Целевые значения обновляются при изменении Narrative Plan. Ответственный: System Analyst.
