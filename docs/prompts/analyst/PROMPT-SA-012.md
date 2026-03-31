# PROMPT-SA-012 — Спецификация HTML Assets Gallery страницы

## Роль и skill-файл

Ты **System Analyst «Life of T»**. Строго следуй `system-analyst-skill.md`.
Перечитай все разделы (docsspecstechnical, assetsspecs, Task Board, SDD), резюмируй обязанности, затем выполни задачу.

> **Зависимость:** TASK-SA-010 выполнена (JSON-схема assets задокументирована).

---

## Задача

Спроектировать **техническую спецификацию раздела Assets Gallery** на Project Status Dashboard — страницы, отображающей все сгенерированные спрайты, атласы и анимации по категориям.

## Контекст

- Ассеты генерируются `asset-generator` модулем в `generated-assets/`
- Структура папок ассетов по категориям:
  - `generated-assets/characters/` — спрайты NPC и главной героини
  - `generated-assets/locations/` — фоны локаций
  - `generated-assets/furniture/` — спрайты мебели
  - `generated-assets/pets/` — спрайты питомцев
  - `generated-assets/ui/` — UI-элементы
- Каждый ассет сопровождается `sprite-atlas.json` с метаданными (rows, frameWidth, frameHeight, variants)
- Страница — статичный HTML, данные из `assets-manifest.json` (генерируется пайплайном)

## Цель

Создать спецификацию `docs/specs/STATUS_PAGE_ASSETS_SPEC.md` описывающую:
1. Структуру `assets-manifest.json`
2. Layout раздела Assets Gallery
3. Компоненты для каждой категории ассетов
4. Отображение анимаций (preview PNG + метаданные)
5. Навигацию между категориями

---

## Требования к Assets Gallery

### Структура `assets-manifest.json`

Пайплайн генерирует этот файл, обходя `generated-assets/`. Схема:

```json
{
  "generatedAt": "ISO-8601",
  "categories": {
    "npc": [
      {
        "id": "tanya",
        "name": "Татьяна (главная героиня)",
        "atlasPath": "assets/characters/tanya/idle_atlas.png",
        "metaPath": "assets/characters/tanya/sprite-atlas.json",
        "animations": [
          { "name": "idle", "rows": 1, "frames": 4, "frameWidth": 48, "frameHeight": 48 }
        ]
      }
    ],
    "locations": [],
    "furniture": [],
    "pets": [],
    "ui": []
  }
}
```

### Layout раздела

```
<section id="assets">
├── <nav class="assets-tabs">  — табы: NPC | Локации | Фурнитура | Питомцы | UI
├── <div class="assets-panel" data-category="npc">   — блок NPC
│   ├── <h2> + счётчик
│   └── <div class="asset-grid">  — сетка карточек ассетов
│       └── <div class="asset-card">
│           ├── <img>  — превью атласа (первый кадр или весь атлас)
│           ├── <div class="asset-meta">  — название, размеры, кол-во кадров
│           └── <div class="animation-list">  — список анимаций ассета
├── <div class="assets-panel" data-category="locations">  — аналогично
├── ... (furniture, pets, ui)
```

### Компонент `.asset-card`

Каждая карточка содержит:
- Превью: PNG-изображение атласа (`<img src="...atlas.png">`)
- Название ассета (из `sprite-atlas.json` или имени директории)
- Метаданные: размер кадра, количество строк/кадров, общий размер файла
- Список анимаций: каждая анимация как бейдж с именем
- При наведении (hover): показывать полную сетку кадров

### Навигация по категориям

- Табы переключают видимость `.assets-panel` без перезагрузки
- Активный таб выделяется цветом
- В заголовке таба — счётчик ассетов данной категории
- URL hash обновляется: `#assets-npc`, `#assets-locations` и т.д.

### Обработка отсутствующих ассетов

- Если категория пуста — показать placeholder «Ассеты ещё не сгенерированы»
- Если `sprite-atlas.json` отсутствует — показать PNG без метаданных
- Битые изображения заменяются placeholder-иконкой

---

## Ответ по SDD

### 1. Specify

Создай `docs/specs/STATUS_PAGE_ASSETS_SPEC.md` с разделами:
- Раздел 1: Схема `assets-manifest.json` (полная JSON Schema с описанием полей)
- Раздел 2: Wireframe раздела Assets Gallery (ASCII-схема layout)
- Раздел 3: Спецификация компонента `.asset-card` (HTML-структура, CSS-классы)
- Раздел 4: Спецификация навигации по категориям (табы, hash-routing)
- Раздел 5: JavaScript API (функции `renderAssetsGallery(manifest)`, `switchCategory(categoryId)`)
- Раздел 6: Правила обработки edge-cases (пустые категории, битые PNG, отсутствие meta)

### 2. Plan

Обнови эпик `EPIC-DEVOPS-001`:
- Отметить Фазу 3 как «В работе»
- Добавить зависимость: TASK-FE-052 зависит от `asset-generator` (сгенерированные ассеты должны присутствовать)

### 3. Task

Создай задачи:

| ID | Тип | Название | Роль | Приоритет |
|----|-----|----------|------|-----------|
| TASK-SA-012 | analytics | Спецификация Assets Gallery страницы | System Analyst | HIGH |
| TASK-BE-021 | backend | Скрипт генерации `assets-manifest.json` из `generated-assets/` | Java Developer | HIGH |
| TASK-FE-052 | frontend | Реализация Assets Gallery HTML+CSS+JS по спецификации SA-012 | JS Developer | HIGH |
| TASK-FE-053 | frontend | Компонент `.asset-card` с hover-preview анимации | JS Developer | MEDIUM |

### 4. Implement

**Критерии готовности:**
- `docs/specs/STATUS_PAGE_ASSETS_SPEC.md` создан
- JSON Schema `assets-manifest.json` описывает все 5 категорий
- Спецификация `.asset-card` покрывает все edge-cases
- JavaScript API задокументирован
- Задачи TASK-BE-021, TASK-FE-052, TASK-FE-053 созданы

---

## Ограничения

- Не реализуй HTML/JS — только спецификацию
- Не трогай логику генерации ассетов (`asset-generator` модуль) — только описывай контракт JSON
- Каждый шаг обосновывай ссылкой на `system-analyst-skill.md`
- Все категории ассетов (NPC, локации, фурнитура, питомцы, UI) обязательны — пустые категории допустимы, но должны иметь placeholder
