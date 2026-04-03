# Техспек: output-mode `docs-preview` в генераторе ассетов

| Поле | Значение |
|------|----------|
| **Путь** | `docs/specs/technical/visual-docs-preview-mode.md` |
| **Компонент** | `asset-generator/` (`ru.lifegame.assets`) |
| **SDD-фаза** | Specify |
| **Дата** | 2026-04-03 |
| **Ответственный** | System Analyst |
| **Исполнитель** | Java Developer |
| **ADR** | [ADR-001](../decisions/ADR-001-visual-docs-data-independence.md) |
| **Задачи** | `tasks/backend/TASK-BE-DOC-001.md` |

---

## 1. Контекст и цель

Генератор ассетов (`ru.lifegame.assets`) в текущей архитектуре поддерживает один output-mode:
генерацию PNG + `sprite-atlas.json`. Необходимо добавить второй режим: **`docs-preview`** —
он генерирует JSON-дескриптор каждой сущности для документационного сайта,
не затрагивая PNG-пайплайн.

Цель: предоставить JavaScript-клиенту документационного сайта структурированные данные всех сущностей (имя, тип, палитра, анимации, ограничения) в машиночитаемом формате без участия человека.

---

## 2. Функциональные требования

### FR-1 — Новый CLI-флаг

Генератор должен поддерживать запуск с параметром:

```
--output-mode=docs-preview
```

Или через Spring Boot application property:

```
assets.output-mode=docs-preview
```

При `docs-preview`:
- Генерация PNG **не выполняется** (или выполняется параллельно — решает Java Developer).
- Дополнительно генерируется `docs-preview.json` (directory: тот же `ASSET_OUTPUT_DIR`).

### FR-2 — Содержимое `docs-preview.json`

Файл `docs-preview.json` — массив объектов. Каждый объект описывает одну сущность.

```jsonc
[
  {
    "id": "tanya",
    "path": "characters/tanya",
    "type": "characters",
    "displayName": "Tanya",
    "spriteAtlasFile": "tanya_idle.png",
    "animations": ["idle", "walk", "sit"],
    "colorPalette": [
      { "name": "skin", "hex": "#F4C89A" },
      { "name": "hair", "hex": "#8B4513" }
    ],
    "constraints": {
      "maxColors": 8,
      "pixelSize": 1,
      "antiAliasing": false
    },
    "abstract": false
  }
]
```

Поля:
- `id` — последний сегмент `path` (`"characters/tanya"` → `"tanya"`).
- `path` — полный path из манифеста.
- `type` — первый сегмент path.
- `displayName` — первая буква заглавная, остальное как есть.
- `spriteAtlasFile` — имя PNG idle-анимации (null если PNG не генерируется).
- `animations` — список из `<animations>` в XML-спеке.
- `colorPalette` — из `<color-palette>` в XML-спеке.
- `constraints` — из `<constraints>` в XML-спеке.
- `abstract` — из атрибута `abstract` в манифесте.

### FR-3 — Источник данных

Генератор получает список сущностей **исключительно** из `specs-manifest.xml`. Никакого списка ID в Java-коде не хардкодится. Итерация по `<entity abstract="false"/>` элементам. Для каждой сущности читается её XML-спек-файл (`{path}/visual-specs.xml` или аналогично).

### FR-4 — Обработка ошибок

| Ситуация | Поведение |
|----------|-----------|
| XML-спек сущности не найден | Запись в отчёт, `abstract` флаг = true, пропуск |
| `<color-palette>` отсутствует | `colorPalette: []` |
| `<animations>` отсутствуют | `animations: []` |
| `<constraints>` отсутствуют | `constraints: null` |

---

## 3. Нефункциональные требования

- **NFR-1**: Режим `docs-preview` не должен изменять поведение `standard`-режима (Clean Architecture: через интерфейс, а не изменение класса).
- **NFR-2**: JSON-вывод должен завершиться за не более **5 секунд** для объёма до 50 сущностей.
- **NFR-3**: Файл `docs-preview.json` валиден согласно JSON Schema (Java Developer может добавить валидацию).
- **NFR-4**: `docs-preview` запускается только через CI; не должен участвовать в продакшн.

---

## 4. Архитектура изменений в `ru.lifegame.assets`

### 4.1. Слой Domain

Добавить интерфейс/порт (Java Developer определяет название):

```
DocPreviewPort.generateDocsPreview(List<AssetSpec> specs) : DocsPreviewResult
```

`DocsPreviewResult` содержит список `EntityDocsDescriptor`:

```
EntityDocsDescriptor {
  id: String
  path: String
  type: String
  displayName: String
  spriteAtlasFile: String | null
  animations: List<String>
  colorPalette: List<ColorEntry>
  constraints: ConstraintsDescriptor | null
  abstract: boolean
}
```

### 4.2. Слой Application

`DocsPreviewUseCase` — оркестрирует получение списка сущностей из манифеста + парсинг их XML-спеков.

### 4.3. Слой Infrastructure

`DocsPreviewJsonWriterAdapter` — сериализует `DocsPreviewResult` в `docs-preview.json`.

### 4.4. Слой Presentation (Application module)

Добавить условный запуск `DocsPreviewUseCase` при `assets.output-mode=docs-preview`.

---

## 5. Сценарий использования

```
CI workflow:
  1. Сборка asset-generator
  2. java -jar life-of-t.jar --output-mode=docs-preview --assets.output-dir=$OUTPUT_DIR
  3. Проверка: [ -f $OUTPUT_DIR/docs-preview.json ]
  4. Проверка: кол-во записей == кол-во abstract=false сущностей в манифесте
  5. Upload docs-preview.json как CI-артефакт
```

---

## 6. Метрики и критерии готовности

| Критерий | Измерение |
|----------|-----------|
| `docs-preview.json` сгенерирован без ошибок | CI: `[ -f docs-preview.json ]` |
| Кол-во объектов == кол-во `abstract=false` в манифесте | CI Python-скрипт |
| `displayName` не пустой | JSON validation или unit-тест |
| Текущий `standard`-режим не нарушен | Регрессия CI |
| Время генерации JSON ≤ 5с | CI-вывод time |
