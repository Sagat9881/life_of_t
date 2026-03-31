# EPIC-DEVOPS-001 — Project Status Dashboard

> **Тип:** Эпик  
> **Статус:** IN_PROGRESS  
> **Дата создания:** 2026-03-31  
> **Ответственный:** System Analyst  
> **Связанная спека:** `docs/specs/PROJECT_METRICS_SPEC.md`  
> **Ссылка на skill:** `system-analyst-skill.md` §3 «Task Board и декомпозиция»

## Цель эпика

Создать автоматически генерируемую HTML-страницу статуса проекта Life of T, публикуемую через GitHub Pages при каждом пуше в `main`. Страница отображает актуальное состояние задач, бэкенда, ассетов, нарратива и CI/CD-пайплайна.

## Фазы реализации

### Фаза 1 — Спецификация метрик (PROMPT-SA-010)

- **Статус:** ✅ DONE
- **Артефакт:** `docs/specs/PROJECT_METRICS_SPEC.md`
- **Задача:** TASK-SA-010
- **Результат:** Определены 5 групп метрик, JSON-схема `metrics.json`, план сбора данных.

### Фаза 2 — HTML Overview страница (PROMPT-SA-011)

- **Статус:** TODO
- **Артефакт:** `docs/status/index.html`
- **Входные данные:** `docs/status/metrics.json`
- **Ожидаемый результат:** Статичная HTML-страница с разделом Overview, отображающая все 5 групп метрик.

### Фаза 3 — Assets Gallery страница (PROMPT-SA-012)

- **Статус:** TODO
- **Артефакт:** `docs/status/gallery.html` или секция на `index.html`
- **Входные данные:** `asset-generator/target/generated-assets/`
- **Ожидаемый результат:** Галерея ассетов с фильтрацией по категориям.

### Фаза 4 — GitHub Actions Pipeline (PROMPT-SA-013)

- **Статус:** TODO
- **Артефакт:** `.github/workflows/status-page.yml`
- **Входные данные:** Скрипты `scripts/metrics/*.py`, репозиторий, GitHub API
- **Ожидаемый результат:** Пайплайн собирает `metrics.json` и публикует страницу при каждом пуше в `main`.

## Диаграмма зависимостей

```
Фаза 1 (Метрики) ──► Фаза 2 (HTML Overview)
                 └──► Фаза 3 (Assets Gallery)
Фаза 2 + Фаза 3  ──► Фаза 4 (Pipeline)
```

## Связанные задачи

| ID | Роль | Фаза | Статус |
|----|------|------|--------|
| TASK-SA-010 | System Analyst | 1 | DONE |
| TASK-BE-018 | Java Developer | 4 | TODO |
| TASK-BE-019 | Java Developer | 4 | TODO |
| TASK-BE-020 | Java Developer | 4 | TODO |
