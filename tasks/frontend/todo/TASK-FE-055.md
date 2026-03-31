# TASK-FE-055 — Сборка финального index.html (Overview + Assets Gallery)

**Тип:** frontend  
**Приоритет:** MEDIUM  
**Статус:** TODO  
**Роль:** JavaScript Developer  
**Связанная спецификация:** `docs/specs/PIPELINE_SPEC.md` (раздел 2.3)  
**Зависимость:** TASK-FE-052, TASK-FE-053, TASK-FE-054

## Описание

Реализовать скрипт инъекции данных в шаблон для job `build-html`.

## Что реализовать

1. `scripts/ci/inject-data.sh` — заменяет плейсхолдеры в шаблоне
2. Job `build-html` в `.github/workflows/build-dashboard.yml`

## Критерии приёмки

- [ ] `output/index.html` генерируется без ошибок
- [ ] `window.__METRICS__` содержит валидный JSON
- [ ] `window.__ASSETS_MANIFEST__` содержит валидный JSON
- [ ] Оба плейсхолдера заменены
- [ ] Страница отображается корректно на `https://sagat9881.github.io/life_of_t/`
