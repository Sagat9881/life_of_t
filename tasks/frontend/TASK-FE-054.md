# TASK-FE-054 — HTML-шаблон с инъекцией metrics.json + assets-manifest.json

| Поле | Значение |
|------|----------|
| **ID** | TASK-FE-054 |
| **Тип** | frontend |
| **Статус** | TODO |
| **Приоритет** | HIGH |
| **Роль** | JS Developer |
| **Дата** | 2026-04-02 |

## Описание

Создать HTML-шаблон `frontend/templates/dashboard.html`, который принимает инлайн-вставку `metrics.json` и `assets-manifest.json` через placeholder-метки и рендерит Project Status Dashboard.

## Связанные спецификации

- `docs/specs/PIPELINE_SPEC.md` §4.1 — схема `metrics.json`
- `docs/specs/PIPELINE_SPEC.md` §4.2 — схема `assets-manifest.json`
- `docs/specs/PIPELINE_SPEC.md` §3.7 — контракт `build-dashboard.sh` (как происходит инъекция)
- TASK-SA-011 — спецификация Overview секции
- TASK-SA-012 — спецификация Assets Gallery секции

## Что нужно создать

`frontend/templates/dashboard.html` — шаблон со следующими требованиями:

1. Содержит `<script>` тег с placeholder-строками:
   ```html
   <script>
     const METRICS = {{METRICS_JSON_INLINE}};
     const ASSETS_MANIFEST = {{ASSETS_MANIFEST_INLINE}};
   </script>
   ```
2. Секция **Overview** (данные из `METRICS`):
   - Task Board: прогресс-бар TODO / IN_PROGRESS / DONE
   - Backend: количество классов по слоям
   - Narrative: количество XML-спецификаций по категориям
   - CI Status: бейдж с цветом (success=зелёный, failure=красный, unknown=серый)
3. Секция **Assets Gallery** (данные из `ASSETS_MANIFEST`):
   - Если `ASSETS_MANIFEST.fallback === true` — показать placeholder «Ассеты генерируются...»
   - Иначе — отрендерить карточки ассетов: изображение (`<img src="path">`), ID, категория, наличие атласа
4. Все данные читаются из JS-переменных (не из fetch-запросов) — шаблон работает как статический HTML

## Ограничения

- Только vanilla JS (без фреймворков, без npm) — статическая страница для GitHub Pages
- CSS — inline или `<style>` тег (без внешних файлов)
- Строки `{{METRICS_JSON_INLINE}}` и `{{ASSETS_MANIFEST_INLINE}}` — точные placeholder-метки для `build-dashboard.sh`
- НЕ использовать `fetch()` или XHR для загрузки данных

## Критерии приёмки

- [ ] `frontend/templates/dashboard.html` создан
- [ ] Оба placeholder `{{METRICS_JSON_INLINE}}` и `{{ASSETS_MANIFEST_INLINE}}` присутствуют
- [ ] При `fallback: true` в ASSETS_MANIFEST показывается placeholder, не ошибка
- [ ] При нулевых значениях в METRICS секции отображаются без ошибок JS
- [ ] Страница корректно открывается в браузере при ручной замене placeholder на валидный JSON

## Метрики

Определяет визуальное представление процессных метрик проекта (см. `docs/metrics/`).
