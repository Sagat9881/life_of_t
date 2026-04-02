# ADR-004: Выбор инструмента публикации GitHub Pages

**ID:** ADR-004  
**Статус:** Принято  
**Дата:** 2026-04-02  
**Автор:** System Analyst  
**Контекст:** TASK-SA-013 — спецификация GitHub Actions Pipeline для Project Status Dashboard  
**Связанные решения:** ADR-005 (формат bash-скриптов vs Python, см. ниже)

---

## Контекст

Пайплайн `build-status-dashboard` (TASK-SA-013) собирает `output/` с `index.html`, `metrics.json`, `assets-manifest.json` и PNG-ассетами, после чего публикует результат на GitHub Pages (`https://sagat9881.github.io/life_of_t/`). Необходимо выбрать инструмент публикации.

## Рассмотренные варианты

### Вариант 1: `peaceiris/actions-gh-pages@v3`

Популярный сторонний action. Принимает директорию `publish_dir` и пушит её содержимое в указанную ветку (`gh-pages`).

**Плюсы:**
- Один шаг в workflow
- `contents: write` — единственное необходимое permission
- Флаг `force_orphan: true` — чистая история ветки `gh-pages`, не раздувает репозиторий
- Произвольная целевая ветка
- Проверенная стабильность (>10k GitHub stars, активная поддержка)
- Не требует предварительной настройки OIDC или environment

**Минусы:**
- Сторонний action (не от GitHub)
- В будущем GitHub может изменить политику в пользу официального стека

### Вариант 2: `actions/configure-pages@v4` + `actions/upload-pages-artifact@v3` + `actions/deploy-pages@v4`

Официальный стек GitHub для Pages deployment.

**Плюсы:**
- Официальная поддержка GitHub
- OIDC-based — не требует PAT

**Минусы:**
- Три отдельных action вместо одного
- Требует permissions `pages: write` + `id-token: write`
- Обязательно включить `github-pages` environment в настройках репозитория заранее
- Нет поддержки `force_orphan`
- Сложнее в первоначальной настройке

### Вариант 3: git push напрямую (bash)

**Плюсы:** полный контроль

**Минусы:** нестабильно, требует обработки merge conflicts, высокая сложность поддержки

## Решение

**Использовать `peaceiris/actions-gh-pages@v3`.**

## Обоснование

1. Минимальная конфигурация снижает вероятность ошибок при первоначальной настройке
2. Флаг `force_orphan: true` критически важен для Dashboard: каждый запуск генерирует полностью новый `output/`, история `gh-pages` не нужна и только растёт
3. Работает с простым `GITHUB_TOKEN` без OIDC-конфигурации репозитория
4. Совместим с текущим состоянием репозитория (нет environment `github-pages`)
5. При необходимости миграции на официальный стек (`actions/deploy-pages@v4`) скрипты сборки не меняются — меняется только job `deploy`

## Последствия

- Java Developer (TASK-BE-022) использует `peaceiris/actions-gh-pages@v3` в YAML workflow
- В настройках репозитория: **Settings → Pages → Source = `gh-pages` branch, `/` (root)**
- Workflow permission: **Settings → Actions → Workflow permissions → Read and write permissions**
- Commit message для gh-pages: `ci: update dashboard [skip ci]` (чтобы не вызывать рекурсивный запуск)

---

## ADR-005: Формат скриптов сбора метрик — bash vs Python

**Статус:** Принято  
**Дата:** 2026-04-02

### Контекст

Скрипты `count-tasks.sh`, `count-backend.sh`, `count-narrative.sh`, `fetch-ci-status.sh` можно реализовать на bash или Python.

### Решение: bash

**Обоснование:**
- `ubuntu-latest` runner включает bash, `find`, `grep`, `wc`, `jq` без дополнительной установки
- Python потребовал бы `actions/setup-python` или риска несовместимости версий
- Скрипты выполняют простые файловые операции (find + grep + wc) — задача bash
- Java Developer знаком с bash в контексте Maven/JVM-сборки

**Исключение:** `build-dashboard.sh` — замена плейсхолдеров в HTML. Разрешается использовать `python3 -c` для inline-замены, если `sed` создаёт проблемы с экранированием JSON. Решение оставлено Java Developer при реализации TASK-BE-022.

**Последствия:**
- Все скрипты в `scripts/` имеют расширение `.sh`
- Шаги workflow запускают скрипты как `bash scripts/script-name.sh`
- Зависимость от `jq` для парсинга GitHub API — `jq` предустановлен на `ubuntu-latest`
