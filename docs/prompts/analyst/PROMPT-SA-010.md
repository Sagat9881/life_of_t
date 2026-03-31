# PROMPT-SA-010 — Спецификация метрик проекта Life of T

## Роль и skill-файл

Ты **System Analyst «Life of T»**. Строго следуй `system-analyst-skill.md`.
Перечитай все разделы (Narrative Plan, docsspecstechnical, Task Board, docsdecisions, docsmetrics, SDD), резюмируй обязанности, затем выполни задачу.

---

## Задача

Спроектировать полную **спецификацию метрик** проекта Life of T, которые будут отображаться на автоматически генерируемой HTML-странице статуса проекта.

## Контекст

- Страница статуса будет собираться GitHub Actions пайплайном при каждом пуше в `main`
- Источники данных: файловая система репозитория (папки `tasks/`, `backend/src/`, `game-content/`, `asset-generator/target/generated-assets/`), GitHub API (статус CI)
- Страница состоит из двух разделов: `Overview` (метрики) и `Assets Gallery` (ассеты)
- Этот промт отвечает только за раздел метрик

## Цель

Создать спецификацию `docs/specs/PROJECT_METRICS_SPEC.md` с:
1. Полным списком верхнеуровневых метрик (5 штук)
2. Для каждой — 3–5 метрик нижнего уровня с описанием
3. Источником данных для каждой метрики (файлы, директории, API)
4. Формулой / логикой вычисления
5. Форматом отображения (число, %, прогресс-бар, статус-бейдж)

---

## Верхнеуровневые метрики (обязательные)

### 1. Task Board Health
Отражает общий прогресс задач по всем ролям.

**Детализация:**
- Всего задач / выполнено / в работе / TODO (источник: папка `tasks/**/*.md`, поле `Статус`)
- % выполнения по роли: backend / frontend / assets / narrative (источник: те же файлы, поле `Тип`)
- Количество задач без зависимостей (готовы к старту)
- Количество заблокированных задач (поле `Зависит от` ссылается на незакрытую задачу)
- Среднее время жизни задачи (дата создания vs дата закрытия через git log)

### 2. Backend Completeness
Отражает зрелость Java-бэкенда.

**Детализация:**
- Количество domain-агрегатов (`backend/src/main/java/ru/lifegame/backend/domain/model/*/`)
- Количество application-сервисов (`application/service/*.java`)
- Количество REST-контроллеров (`infrastructure/web/controller/*.java`)
- Наличие unit-тестов: % покрытых классов (`backend/src/test/`)
- Количество нарративных спецификаций, загружаемых без хардкода (связь с TASK-BE-015/016)

### 3. Assets Coverage
Отражает наполненность визуальных ассетов.

**Детализация:**
- Всего сгенерированных атласов (`.png` файлов в `generated-assets/`)
- Количество анимаций по категориям: NPC / locations / furniture / pets / ui
- Количество атласов с `sprite-atlas.json` метаданными
- Количество локаций с полным набором (idle + variants)
- Количество NPC с полным набором анимаций

### 4. Narrative Coverage
Отражает наполненность нарративного контента.

**Детализация:**
- Количество квестов в `game-content/` (`quest/*.xml`)
- Количество конфликтов (`conflicts/*.xml`)
- Количество NPC-блоков (`npc/*.xml`)
- Количество world-events (`world-events/*.xml`)
- Количество блоков с манифестом (`manifest.xml` в директории блока)

### 5. CI/CD Health
Отражает состояние пайплайна и сборки.

**Детализация:**
- Статус последнего GitHub Actions run (GitHub API: `/repos/{owner}/{repo}/actions/runs`)
- Время последней успешной сборки
- Статус Docker-образа (наличие и дата `Dockerfile`)
- Наличие и актуальность `docker-compose.yml`
- Статус GitHub Pages (доступность страницы статуса)

---

## Ответ по SDD

### 1. Specify

Создай файл `docs/specs/PROJECT_METRICS_SPEC.md` со следующими разделами:
- Раздел 1: Глоссарий метрик (определение каждого термина)
- Раздел 2: Таблица метрик (ID, название, уровень, источник данных, формула, формат)
- Раздел 3: Схема сбора данных (какой скрипт/шаг пайплайна собирает каждую группу)
- Раздел 4: JSON-схема выходного файла `metrics.json` (который генерирует пайплайн и читает HTML-страница)

Пример JSON-схемы:
```json
{
  "generatedAt": "ISO-8601",
  "taskBoard": {
    "total": 0, "done": 0, "inProgress": 0, "todo": 0,
    "byRole": { "backend": {}, "frontend": {}, "assets": {}, "narrative": {} },
    "blocked": 0, "readyToStart": 0
  },
  "backend": {
    "aggregates": 0, "services": 0, "controllers": 0, "testCoverage": 0
  },
  "assets": {
    "totalAtlases": 0,
    "byCategory": { "npc": 0, "locations": 0, "furniture": 0, "pets": 0, "ui": 0 }
  },
  "narrative": {
    "quests": 0, "conflicts": 0, "npc": 0, "worldEvents": 0, "withManifest": 0
  },
  "cicd": {
    "lastRunStatus": "success|failure|pending",
    "lastRunAt": "ISO-8601",
    "pagesUrl": ""
  }
}
```

### 2. Plan

Обнови `narrativeplans/` (или `docs/plans/`) добавив запись:
- Эпик: `EPIC-DEVOPS-001 — Project Status Dashboard`
- Фаза 1: Спецификация метрик (этот промт)
- Фаза 2: HTML Overview страница (PROMPT-SA-011)
- Фаза 3: Assets Gallery страница (PROMPT-SA-012)
- Фаза 4: GitHub Actions Pipeline (PROMPT-SA-013)

### 3. Task

Создай задачи на Task Board:

| ID | Тип | Название | Роль | Приоритет |
|----|-----|----------|------|-----------|
| TASK-SA-010 | analytics | Спецификация метрик проекта + JSON-схема | System Analyst | HIGH |
| TASK-BE-018 | backend | Скрипт сбора метрик Task Board из `tasks/` | Java Developer | MEDIUM |
| TASK-BE-019 | backend | Скрипт сбора метрик backend-кода (счётчики классов) | Java Developer | MEDIUM |
| TASK-BE-020 | backend | Скрипт сбора метрик Assets (счётчики PNG/JSON) | Java Developer | MEDIUM |

### 4. Implement

**Критерии готовности:**
- `docs/specs/PROJECT_METRICS_SPEC.md` создан и содержит все 5 групп метрик
- JSON-схема `metrics.json` задокументирована
- Каждая метрика имеет явный источник данных и формулу
- Задачи TASK-BE-018..020 созданы в `tasks/backend/`
- Документ прошёл проверку: нет метрик без источника данных

---

## Ограничения

- Не описывай реализацию HTML или пайплайна — только метрики и схему данных
- Каждый шаг обосновывай ссылкой на `system-analyst-skill.md`
- Не выполняй работу Java-разработчика (реализацию скриптов)
