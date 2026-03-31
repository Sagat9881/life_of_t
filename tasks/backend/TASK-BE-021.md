# TASK-BE-021 — Скрипт генерации `assets-manifest.json`

**Тип:** backend  
**Приоритет:** HIGH  
**Статус:** TODO  
**Роль:** Java Developer  
**Связанная спецификация:** `docs/specs/STATUS_PAGE_ASSETS_SPEC.md` (разделы 2, 7.3, 7.4)  
**Зависимость:** нет (может стартовать сразу)

## Описание

Реализовать в модуле `asset-generator` скрипт/компонент, который:

1. Обходит директории `generated-assets/characters/`, `generated-assets/locations/`, `generated-assets/furniture/`, `generated-assets/pets/`, `generated-assets/ui/`.
2. Для каждого ассета (поддиректория с PNG-атласом):
   - Читает `sprite-atlas.json` если присутствует; заполняет `animations`, `metaPath`.
   - Если `sprite-atlas.json` отсутствует: `metaPath: null`, `animations: []`.
   - Заполняет `fileSizeBytes` из размера PNG-файла.
3. Маппинг директорий → ключи `categories`: согласно таблице в спецификации (раздел 2.3).
4. Записывает результирующий JSON в `assets-manifest.json` в корне статики.
5. **Обязательно** включает в выход `assets/ui/placeholder.png` (см. раздел 7.3 спецификации).

## Контракт выходных данных

Сгенерированный `assets-manifest.json` **обязан** проходить JSON Schema из `docs/specs/STATUS_PAGE_ASSETS_SPEC.md`, раздел 2.1.

## Критерии приёмки

- [ ] `assets-manifest.json` генерируется без ошибок при пустых категориях
- [ ] `assets-manifest.json` валиден по JSON Schema (раздел 2.1)
- [ ] Все 5 категорий присутствуют в ключе `categories` (даже если пусты)
- [ ] `metaPath: null` если `sprite-atlas.json` отсутствует
- [ ] `fileSizeBytes` корректен
- [ ] `assets/ui/placeholder.png` присутствует в выходных данных
- [ ] Скрипт идемпотентен (повторный запуск перезаписывает файл)
