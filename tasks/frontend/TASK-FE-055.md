# TASK-FE-055 — Сборка финального index.html (объединение Overview + Assets Gallery)

**Тип:** frontend  
**Приоритет:** MEDIUM  
**Статус:** TODO  
**Роль:** JavaScript Developer  
**Связанная спецификация:** `docs/specs/PIPELINE_SPEC.md` (раздел 2.3); `docs/specs/STATUS_PAGE_OVERVIEW_SPEC.md`; `docs/specs/STATUS_PAGE_ASSETS_SPEC.md`  
**Зависимость:** TASK-FE-052, TASK-FE-053, TASK-FE-054

## Описание

Реализовать скрипт инъекции данных в шаблон, который выполняется в job `build-html`.

## Что реализовать

1. **`scripts/ci/inject-data.sh`** (или `inject-data.js` — по усмотрению JS Developer, Bash-предпочтительно):
   - Принимает пути к `index.template.html`, `metrics.json`, `assets-manifest.json`.
   - Заменяет `<!-- METRICS_JSON_PLACEHOLDER -->` на `<script>window.__METRICS__ = JSON_CONTENT;</script>`.
   - Заменяет `<!-- ASSETS_MANIFEST_PLACEHOLDER -->` на `<script>window.__ASSETS_MANIFEST__ = JSON_CONTENT;</script>`.
   - Записывает результат в `output/index.html`.
2. Добавить job `build-html` в `.github/workflows/build-dashboard.yml` согласно разделу 2.3 `PIPELINE_SPEC.md`.

## Критерии приёмки

- [ ] `output/index.html` генерируется без ошибок
- [ ] `window.__METRICS__` содержит валидный JSON
- [ ] `window.__ASSETS_MANIFEST__` содержит валидный JSON
- [ ] Оба плейсхолдера заменены (ни одного не осталось в `output/index.html`)
- [ ] `output/metrics.json` и `output/assets-manifest.json` скопированы
- [ ] `output/assets/` содержит PNG-ассеты (или пустая директория если ассетов нет)
- [ ] Страница отображается корректно на `https://sagat9881.github.io/life_of_t/`
