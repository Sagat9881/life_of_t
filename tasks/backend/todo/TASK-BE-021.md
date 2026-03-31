# TASK-BE-021 — Скрипт генерации `assets-manifest.json`

**Тип:** backend  
**Приоритет:** HIGH  
**Статус:** TODO  
**Роль:** Java Developer  
**Связанная спецификация:** `docs/specs/STATUS_PAGE_ASSETS_SPEC.md`  
**Зависимость:** нет (может стартовать сразу)

## Описание

Реализовать в модуле `asset-generator` компонент, который обходит `generated-assets/` по категориям и генерирует `assets-manifest.json`.

## Критерии приёмки

- [ ] `assets-manifest.json` генерируется без ошибок при пустых категориях
- [ ] `assets-manifest.json` валиден по JSON Schema (раздел 2.1)
- [ ] Все 5 категорий присутствуют в ключе `categories` (даже если пусты)
- [ ] `metaPath: null` если `sprite-atlas.json` отсутствует
- [ ] `fileSizeBytes` корректен
- [ ] `assets/ui/placeholder.png` присутствует в выходных данных
- [ ] Скрипт идемпотентен
