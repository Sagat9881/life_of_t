# TASK-FE-054 — HTML-шаблон с инъекцией metrics.json + assets-manifest.json

**Тип:** frontend  
**Приоритет:** HIGH  
**Статус:** TODO  
**Роль:** JavaScript Developer  
**Связанная спецификация:** `docs/specs/PIPELINE_SPEC.md` (раздел 2.3, шаг inject-data)  
**Зависимость:** TASK-FE-052, TASK-FE-055 (базовая страница должна существовать)

## Описание

Создать `frontend/status/index.template.html` — HTML-шаблон, в который CI-пайплайн вставляет данные.

## Что реализовать

1. **`frontend/status/index.template.html`** — полноценная HTML-страница Dashboard.
2. Содержит плейсхолдер `<!-- METRICS_JSON_PLACEHOLDER -->` строго до закрывающего `</head>`.
3. Содержит плейсхолдер `<!-- ASSETS_MANIFEST_PLACEHOLDER -->` рядом с первым.
4. После инъекции скрипт `inject-data` (CI) заменяет плейсхолдеры на:
   ```html
   <script>window.__METRICS__ = { ...metrics.json content... };</script>
   <script>window.__ASSETS_MANIFEST__ = { ...assets-manifest.json content... };</script>
   ```
5. Клиентский JS в шаблоне читает `window.__METRICS__` и `window.__ASSETS_MANIFEST__` для рендера (не делает `fetch`).

## Ограничение

Шаблон НЕ должен делать `fetch('metrics.json')` или `fetch('assets-manifest.json')` — данные уже инжектированы в HTML.

## Критерии приёмки

- [ ] Файл `frontend/status/index.template.html` существует
- [ ] Оба плейсхолдера присутствуют в файле (точные строки)
- [ ] После ручной замены плейсхолдеров страница корректно рендерит Overview и Assets Gallery
- [ ] Страница не делает внешних fetch-запросов к JSON-файлам
- [ ] Совместимость: Chrome 100+, Firefox 100+, Safari 15+
