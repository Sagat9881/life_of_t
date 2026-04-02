# TASK-FE-054 — HTML-шаблон с инъекцией metrics.json + assets-manifest.json

**Тип:** frontend  
**Приоритет:** HIGH  
**Статус:** TODO  
**Роль:** JavaScript Developer  
**Связанная спецификация:** `docs/specs/PIPELINE_SPEC.md` (раздел 2.3)  
**Зависимость:** TASK-FE-052, TASK-FE-055

## Описание

Создать `frontend/status/index.template.html` — HTML-шаблон для инъекции данных CI-пайплайном.

## Критерии приёмки

- [ ] Файл `frontend/status/index.template.html` существует
- [ ] Присутствует плейсхолдер `<!-- METRICS_JSON_PLACEHOLDER -->`
- [ ] Присутствует плейсхолдер `<!-- ASSETS_MANIFEST_PLACEHOLDER -->`
- [ ] После замены плейсхолдеров страница корректно рендерит Overview и Assets Gallery
- [ ] Страница не делает внешних fetch-запросов к JSON-файлам
- [ ] Совместимость: Chrome 100+, Firefox 100+, Safari 15+
