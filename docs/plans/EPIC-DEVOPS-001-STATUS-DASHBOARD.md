# EPIC-DEVOPS-001 — Project Status Dashboard

> **Тип:** Эпик  
> **Статус:** IN_PROGRESS  
> **Дата создания:** 2026-03-31  
> **Ответственный:** System Analyst  
> **Связанная спека:** `docs/specs/PROJECT_METRICS_SPEC.md`, `docs/specs/STATUS_PAGE_OVERVIEW_SPEC.md`

## Цель эпика

Создать автоматически генерируемую HTML-страницу статуса проекта Life of T, публикуемую через GitHub Pages при каждом пуше в `main`. Страница отображает актуальное состояние задач, бэкенда, ассетов, нарратива и CI/CD-пайплайна.

## Фазы реализации

### Фаза 1 — Спецификация метрик (PROMPT-SA-010)

- **Статус:** ✅ DONE
- **Артефакт:** `docs/specs/PROJECT_METRICS_SPEC.md`
- **Задача:** TASK-SA-010
- **Результат:** Определены 5 групп метрик, JSON-схема `metrics.json`, план сбора данных.

### Фаза 2 — HTML Overview страница (PROMPT-SA-011)

- **Статус:** 🔄 IN_PROGRESS
- **Артефакт:** `docs/specs/STATUS_PAGE_OVERVIEW_SPEC.md` ✅
- **Задачи:** TASK-SA-011 (DONE) → TASK-FE-050, TASK-FE-051 (TODO)
- **Ожидаемый результат:** Статичная HTML-страница `docs/status/index.html` с разделом Overview, отображающим все 5 групп метрик с drilldown-панелями.

### Фаза 3 — Assets Gallery страница (PROMPT-SA-012)

- **Статус:** TODO
- **Артефакт:** `docs/status/gallery.html` или секция на `index.html`
- **Входные данные:** `asset-generator/target/generated-assets/`
- **Ожидаемый результат:** Галерея ассетов с фильтрацией по категориям.
- **Зависимость:** Фаза 2 должна быть завершена (общий `status.css` переиспользуется)

### Фаза 4 — GitHub Actions Pipeline (PROMPT-SA-013)

- **Статус:** TODO
- **Артефакт:** `.github/workflows/status-page.yml`
- **Входные данные:** Скрипты `scripts/metrics/*.py`, репозиторий, GitHub API
- **Ожидаемый результат:** Пайплайн собирает `metrics.json` и публикует страницу при каждом пуше в `main`.
- **Зависимость:** Фазы 2 + 3 (HTML-файлы должны существовать)

## Диаграмма зависимостей

```
Фаза 1 (Метрики) ──► Фаза 2 (HTML Overview)
                 └──► Фаза 3 (Assets Gallery)
Фаза 2 + Фаза 3  ──► Фаза 4 (Pipeline)
```

## Связанные задачи

| ID | Роль | Фаза | Статус |
|----|------|------|--------|
| TASK-SA-010 | System Analyst | 1 | ✅ DONE |
| TASK-BE-018 | Java Developer | 4 | TODO |
| TASK-BE-019 | Java Developer | 4 | TODO |
| TASK-BE-020 | Java Developer | 4 | TODO |
| TASK-SA-011 | System Analyst | 2 | ✅ DONE |
| TASK-FE-050 | JS Developer | 2 | TODO |
| TASK-FE-051 | JS Developer | 2 | TODO |
