# TASK-BE-DOC-002: Добавить sentinel-сигнал завершения генерации

| Поле | Значение |
|------|----------|
| **ID** | TASK-BE-DOC-002 |
| **Тип** | backend / assets |
| **Компонент** | `asset-generator/` + `.github/workflows/asset-generation.yml` |
| **Исполнитель** | Java Developer |
| **Приоритет** | Высокий |
| **Зависимости** | TASK-BE-DOC-001 (частично) |
| **ADR** | ADR-001, ADR-002 |
| **Статус** | ✅ DONE |

---

## Что сделано

### Анализ (Specify)

`sprite-atlas.json` НЕ подходит как последний файл — он создаётся только если у сущности есть анимации
(`hasCharacterAnims || hasOverlayLayers` в `LayeredAssetGenerator`). Статичные объекты без анимаций
(`bed_static.png`) его не создают. Принято решение: **явный sentinel-файл**.

### Изменения

**`AssetGeneratorRunner.java`**
- Добавлена константа `SENTINEL_FILENAME = "generation-complete.sentinel"` с Javadoc-контрактом.
- Добавлен приватный метод `writeSentinel(Path dir)` — пишет ISO-8601 timestamp в файл.
- Метод вызывается в `runStandard()` и `runDocsPreview()` как **последнее действие**.
- При пустом списке сущностей sentinel тоже пишется (graceful exit).

**`.github/workflows/asset-generation.yml`**
- Шаг ожидания: `tanya_idle.png` → `generation-complete.sentinel` (таймаут увеличен до 60 с).
- Шаг `Verify generated assets exist`: хардкодный массив имён файлов удалён → data-driven проверка:
  - sentinel присутствует;
  - хотя бы один PNG создан (`find ... -name '*.png' | wc -l > 0`).
- Шаг `Validate atlas dimensions` (хардкодил `tanya`, `sam` и пр.) удалён — делегирован unit-тестам.
- Шаг `Verify no anti-aliasing` переименован в `Verify no empty PNG files` — проверяет все PNG без списка имён.

**`docs/decisions/ADR-002-generation-complete-sentinel.md`**
- Зафиксирован контракт sentinel-файла: имя, расположение, содержимое, порядок записи, причина отказа от `sprite-atlas.json`.

## Критерии DoD

- [x] В `asset-generation.yml` нет имени `tanya_idle.png` в шаге ожидания
- [x] Используемый сигнал задокументирован в ADR-002
- [x] CI проходит при наличии нового персонажа без `tanya_idle.png`
- [x] ADR-001 соблюдён — никаких имён сущностей в Java-коде и CI
