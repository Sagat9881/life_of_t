# PROMPT-SA-013 — Спецификация GitHub Actions Pipeline для Project Status Dashboard

## Роль и skill-файл

Ты **System Analyst «Life of T»**. Строго следуй `system-analyst-skill.md`.
Перечитай все разделы (docsspecstechnical, docsdecisions, Task Board, SDD), резюмируй обязанности, затем выполни задачу.

> **Зависимости:** TASK-SA-010, TASK-SA-011, TASK-SA-012 должны быть выполнены (спецификации метрик, Overview и Assets Gallery готовы).

---

## Задача

Спроектировать **техническую спецификацию GitHub Actions пайплайна**, который автоматически собирает и публикует Project Status Dashboard при каждом пуше в `main`.

## Контекст

- Репозиторий: `Sagat9881/life_of_t`, ветка `main`
- Пайплайн должен собирать:
  1. `metrics.json` — данные о задачах, коде, нарративе (из репозитория)
  2. `assets-manifest.json` — перечень сгенерированных ассетов
  3. `index.html` — итоговая страница (Overview + Assets Gallery)
- Публикация: GitHub Pages (ветка `gh-pages` или директория `/docs`)
- Страница должна быть доступна по `https://sagat9881.github.io/life_of_t/`

## Цель

Создать спецификацию `docs/specs/PIPELINE_SPEC.md` описывающую:
1. Триггеры и условия запуска пайплайна
2. Все шаги (jobs и steps) с описанием входов/выходов
3. Скрипты сбора данных (что делает каждый шаг)
4. Структуру артефактов и способ публикации
5. Стратегию обработки ошибок

---

## Требования к пайплайну

### Триггеры

```yaml
on:
  push:
    branches: [main]
  workflow_dispatch:  # ручной запуск
  schedule:
    - cron: '0 6 * * *'  # ежедневно в 06:00 UTC
```

### Структура jobs

```
workflow: build-status-dashboard
├── job: collect-metrics
│   ├── step: checkout
│   ├── step: count-tasks (bash: парсинг tasks/**/*.md)
│   ├── step: count-backend-classes (bash: find backend/src/ -name '*.java')
│   ├── step: count-narrative-specs (bash: find game-content/ -name '*.xml')
│   ├── step: fetch-ci-status (GitHub API)
│   └── step: write metrics.json
│
├── job: collect-assets (depends on: collect-metrics)
│   ├── step: checkout
│   ├── step: build asset-generator (mvn compile в asset-generator/)
│   ├── step: run asset-generator (генерация PNG)
│   ├── step: scan generated-assets/ → assets-manifest.json
│   └── step: write assets-manifest.json
│
├── job: build-html (depends on: collect-metrics, collect-assets)
│   ├── step: checkout
│   ├── step: inject metrics.json + assets-manifest.json в HTML-шаблон
│   ├── step: copy generated-assets/ → output/assets/
│   └── step: write output/index.html
│
└── job: deploy (depends on: build-html)
    ├── step: checkout gh-pages
    └── step: publish output/ → GitHub Pages
```

### Скрипты сбора метрик (bash)

**Сбор задач (`count-tasks.sh`):**
```bash
# Считает задачи по статусу из tasks/**/*.md
# Ищет строку "| **Статус** | TODO|IN_PROGRESS|DONE "
# Выводит JSON: {"todo": N, "inProgress": N, "done": N}
```

**Сбор классов бэкенда (`count-backend.sh`):**
```bash
# Считает Java-файлы по директориям
# domain/model/**/*.java → aggregates
# application/service/**/*.java → services  
# infrastructure/web/controller/**/*.java → controllers
# src/test/**/*.java → tests
```

**Сбор нарративных спецификаций (`count-narrative.sh`):**
```bash
# Считает XML-файлы в game-content/
# quest/*.xml → quests
# conflicts/*.xml → conflicts
# npc/*.xml → npc
# world-events/*.xml → worldEvents
# manifest.xml → withManifest
```

**Сканирование ассетов (`scan-assets.sh`):**
```bash
# Обходит generated-assets/ по категориям
# Для каждого PNG читает sprite-atlas.json (если есть)
# Генерирует assets-manifest.json
```

### Публикация

- Использовать `peaceiris/actions-gh-pages@v3` или `actions/deploy-pages@v4`
- Публиковать директорию `output/`
- Структура `output/`:
  ```
  output/
  ├── index.html          — главная страница
  ├── metrics.json        — данные метрик
  ├── assets-manifest.json — манифест ассетов
  └── assets/             — скопированные PNG ассетов
  ```

### Обработка ошибок

- Если `asset-generator` не собрался — пайплайн продолжает без ассетов (Assets Gallery показывает placeholder)
- Если GitHub API недоступен — CI/CD метрика помечается как `unknown`
- Если `tasks/` пуста — Task Board показывает нули без ошибки
- `continue-on-error: true` только для опциональных шагов

---

## Ответ по SDD

### 1. Specify

Создай `docs/specs/PIPELINE_SPEC.md` с разделами:
- Раздел 1: Архитектура пайплайна (диаграмма jobs и зависимостей)
- Раздел 2: Спецификация каждого job (inputs, outputs, environment vars)
- Раздел 3: Спецификация bash-скриптов (алгоритм, входные файлы, выходной JSON)
- Раздел 4: Структура артефактов (схема директории `output/`)
- Раздел 5: Конфигурация GitHub Pages (настройки репозитория, permissions)
- Раздел 6: Стратегия обработки ошибок и fallback-значения
- Раздел 7: ADR — решение об инструменте публикации (gh-pages vs deploy-pages)

### 2. Plan

Обнови эпик `EPIC-DEVOPS-001`:
- Отметить Фазу 4 как «В работе»
- Зафиксировать ADR: «Используем peaceiris/actions-gh-pages для совместимости с существующим workflow»
- Добавить в `docsdecisions`: решение о формате bash-скриптов vs Python

### 3. Task

Создай задачи:

| ID | Тип | Название | Роль | Приоритет |
|----|-----|----------|------|-----------|
| TASK-SA-013 | analytics | Спецификация GitHub Actions Pipeline | System Analyst | HIGH |
| TASK-BE-022 | backend | Bash-скрипты сбора метрик (tasks, backend, narrative) | Java Developer | HIGH |
| TASK-BE-023 | backend | Bash-скрипт сканирования ассетов → assets-manifest.json | Java Developer | HIGH |
| TASK-FE-054 | frontend | HTML-шаблон с инъекцией metrics.json + assets-manifest.json | JS Developer | HIGH |
| TASK-FE-055 | frontend | Сборка финального index.html (объединение Overview + Assets Gallery) | JS Developer | MEDIUM |

### 4. Implement

**Критерии готовности:**
- `docs/specs/PIPELINE_SPEC.md` создан и покрывает все 4 jobs
- Bash-скрипты специфицированы (алгоритм + пример выходного JSON)
- ADR о выборе инструмента публикации задокументирован в `docsdecisions`
- Задачи TASK-BE-022..023, TASK-FE-054..055 созданы
- Структура `output/` задокументирована
- Описан способ настройки GitHub Pages в репозитории (Settings → Pages → Source)

---

## Ограничения

- Не пиши YAML workflow — только спецификацию (YAML создаёт DevOps/Java по задаче TASK-BE-022)
- Не реализуй bash-скрипты — только алгоритм и контракт
- Каждый шаг обосновывай ссылкой на `system-analyst-skill.md`
- Пайплайн не должен требовать дополнительных платных GitHub Actions credits (использовать только ubuntu-latest)
