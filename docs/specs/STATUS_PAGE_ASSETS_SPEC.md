# STATUS_PAGE_ASSETS_SPEC — Спецификация раздела Assets Gallery

**ID:** SPEC-SA-012  
**Версия:** 1.0  
**Дата:** 2026-03-31  
**Зависимость:** TASK-SA-010 (JSON-схема assets задокументирована)  
**Связанные задачи:** TASK-BE-021, TASK-FE-052, TASK-FE-053  
**Автор:** System Analyst  
**Методология:** SDD (Specify → Plan → Task → Implement) — см. `system-analyst-skill.md`, раздел 5

---

## 1. Контекст и цель

Раздел **Assets Gallery** является частью Project Status Dashboard — статичной HTML-страницы,
которая отображает все сгенерированные спрайты, атласы и анимации по категориям.

- Данные загружаются из `assets-manifest.json`, генерируемого пайплайном обхода `generated-assets/`.
- Страница предназначена для разработчиков и QA: позволяет визуально проверить результаты `asset-generator`.
- Не требует серверного бэкенда — только статичные файлы + JSON.

---

## 2. Схема `assets-manifest.json`

> Контракт между `asset-generator` (Java) и Assets Gallery (JS/HTML).  
> Аналитик описывает схему; реализацию генератора выполняет Java Developer (TASK-BE-021).

### 2.1. JSON Schema (Draft-07)

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "AssetsManifest",
  "type": "object",
  "required": ["generatedAt", "categories"],
  "properties": {
    "generatedAt": {
      "type": "string",
      "format": "date-time",
      "description": "ISO-8601 timestamp момента генерации манифеста"
    },
    "categories": {
      "type": "object",
      "required": ["npc", "locations", "furniture", "pets", "ui"],
      "properties": {
        "npc":       { "$ref": "#/definitions/assetList" },
        "locations": { "$ref": "#/definitions/assetList" },
        "furniture": { "$ref": "#/definitions/assetList" },
        "pets":      { "$ref": "#/definitions/assetList" },
        "ui":        { "$ref": "#/definitions/assetList" }
      },
      "additionalProperties": false
    }
  },
  "definitions": {
    "assetList": {
      "type": "array",
      "items": { "$ref": "#/definitions/assetEntry" }
    },
    "assetEntry": {
      "type": "object",
      "required": ["id", "name", "atlasPath"],
      "properties": {
        "id": {
          "type": "string",
          "description": "Уникальный идентификатор ассета (slug директории)"
        },
        "name": {
          "type": "string",
          "description": "Человекочитаемое название ассета"
        },
        "atlasPath": {
          "type": "string",
          "description": "Относительный путь к PNG-атласу от корня статики"
        },
        "metaPath": {
          "type": ["string", "null"],
          "description": "Относительный путь к sprite-atlas.json; null если файл отсутствует"
        },
        "fileSizeBytes": {
          "type": ["integer", "null"],
          "description": "Размер атласа в байтах; null если недоступен"
        },
        "animations": {
          "type": "array",
          "items": { "$ref": "#/definitions/animationEntry" },
          "description": "Список анимаций ассета; пустой массив если метаданные отсутствуют"
        }
      },
      "additionalProperties": false
    },
    "animationEntry": {
      "type": "object",
      "required": ["name", "rows", "frames", "frameWidth", "frameHeight"],
      "properties": {
        "name": {
          "type": "string",
          "description": "Имя анимации (idle, walk, run, …)"
        },
        "rows": {
          "type": "integer",
          "minimum": 1,
          "description": "Количество строк в атласе для данной анимации"
        },
        "frames": {
          "type": "integer",
          "minimum": 1,
          "description": "Суммарное количество кадров анимации"
        },
        "frameWidth": {
          "type": "integer",
          "minimum": 1,
          "description": "Ширина одного кадра в пикселях"
        },
        "frameHeight": {
          "type": "integer",
          "minimum": 1,
          "description": "Высота одного кадра в пикселях"
        }
      },
      "additionalProperties": false
    }
  }
}
```

### 2.2. Пример валидного манифеста

```json
{
  "generatedAt": "2026-03-31T16:00:00Z",
  "categories": {
    "npc": [
      {
        "id": "tanya",
        "name": "Татьяна (главная героиня)",
        "atlasPath": "assets/characters/tanya/idle_atlas.png",
        "metaPath": "assets/characters/tanya/sprite-atlas.json",
        "fileSizeBytes": 48320,
        "animations": [
          { "name": "idle", "rows": 1, "frames": 4, "frameWidth": 48, "frameHeight": 48 },
          { "name": "walk", "rows": 1, "frames": 8, "frameWidth": 48, "frameHeight": 48 }
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

### 2.3. Маппинг директорий → категории манифеста

| Директория                       | Ключ `categories` |
|----------------------------------|--------------------|
| `generated-assets/characters/`   | `npc`              |
| `generated-assets/locations/`    | `locations`        |
| `generated-assets/furniture/`    | `furniture`        |
| `generated-assets/pets/`         | `pets`             |
| `generated-assets/ui/`           | `ui`               |

---

## 3. Wireframe раздела Assets Gallery

```
┌─────────────────────────────────────────────────────────────────┐
│  <section id="assets">                                          │
│                                                                 │
│  ┌─ <nav class="assets-tabs"> ──────────────────────────────┐  │
│  │  [NPC (3)] | [Локации (0)] | [Мебель (5)] | [Питомцы (2)] | [UI (8)] │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
│  ┌─ <div class="assets-panel" data-category="npc"> ──────────┐  │
│  │  <h2>NPC <span class="count">3</span></h2>                │  │
│  │                                                           │  │
│  │  ┌─ <div class="asset-grid"> ───────────────────────────┐ │  │
│  │  │                                                       │ │  │
│  │  │  ┌──────────┐  ┌──────────┐  ┌──────────┐           │ │  │
│  │  │  │asset-card│  │asset-card│  │asset-card│           │ │  │
│  │  │  │          │  │          │  │          │           │ │  │
│  │  │  │  [img]   │  │  [img]   │  │  [img]   │           │ │  │
│  │  │  │  .meta   │  │  .meta   │  │  .meta   │           │ │  │
│  │  │  │  .anims  │  │  .anims  │  │  .anims  │           │ │  │
│  │  │  └──────────┘  └──────────┘  └──────────┘           │ │  │
│  │  └───────────────────────────────────────────────────────┘ │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                 │
│  <!-- остальные .assets-panel скрыты (display:none) -->        │
└─────────────────────────────────────────────────────────────────┘
```

---

## 4. Спецификация компонента `.asset-card`

> Реализацию HTML+CSS выполняет JS Developer (TASK-FE-052, TASK-FE-053).

### 4.1. HTML-структура

```html
<div class="asset-card" data-asset-id="tanya" data-category="npc">

  <!-- Превью атласа -->
  <div class="asset-preview">
    <img
      class="asset-atlas-img"
      src="assets/characters/tanya/idle_atlas.png"
      alt="Татьяна (главная героиня)"
      loading="lazy"
      onerror="this.src='assets/ui/placeholder.png'; this.classList.add('broken')"
    />
  </div>

  <!-- Метаданные -->
  <div class="asset-meta">
    <h3 class="asset-name">Татьяна (главная героиня)</h3>
    <ul class="asset-details">
      <li class="detail-frame-size">Кадр: 48×48 px</li>
      <li class="detail-frames">Кадров: 4</li>
      <li class="detail-rows">Строк: 1</li>
      <li class="detail-filesize">Файл: 47 KB</li>
    </ul>
  </div>

  <!-- Список анимаций -->
  <div class="animation-list">
    <span class="anim-badge" data-anim="idle">idle</span>
    <span class="anim-badge" data-anim="walk">walk</span>
  </div>

</div>
```

### 4.2. CSS-классы и поведение

| Класс / селектор               | Назначение                                                        |
|-------------------------------|-------------------------------------------------------------------|
| `.asset-card`                 | Контейнер карточки; grid-item                                     |
| `.asset-preview`              | Обёртка изображения; overflow:hidden для hover-эффекта           |
| `.asset-atlas-img`            | Само изображение; `object-fit: contain`                          |
| `.asset-atlas-img.broken`     | Устанавливается через `onerror`; отображает placeholder-иконку   |
| `.asset-card:hover .asset-preview` | Масштабировать изображение до 100% ширины карточки (zoom-in) |
| `.asset-meta`                 | Блок с текстовыми метаданными                                     |
| `.asset-name`                 | Заголовок (h3) с именем ассета                                   |
| `.asset-details`              | Список характеристик (кадр, размер, файл)                        |
| `.animation-list`             | Flex-контейнер бейджей анимаций                                   |
| `.anim-badge`                 | Бейдж одной анимации; rounded pill                               |

### 4.3. Правила отображения метаданных

- `detail-frame-size`: форматировать как `«ширина × высота px»`; берётся из первой анимации в массиве `animations`.
- `detail-frames`: суммарное количество кадров всех анимаций (или `N/A` если `animations` пуст).
- `detail-filesize`: конвертировать `fileSizeBytes` → KB с округлением до 1 знака; если `null` — отобразить `«—»`.
- `anim-badge`: один бейдж на каждый элемент массива `animations`.

---

## 5. Спецификация навигации по категориям

### 5.1. Структура табов

```html
<nav class="assets-tabs" role="tablist">
  <button class="tab-btn active"
          role="tab"
          data-category="npc"
          aria-controls="panel-npc"
          aria-selected="true">
    NPC <span class="tab-count">3</span>
  </button>
  <button class="tab-btn"
          role="tab"
          data-category="locations"
          aria-controls="panel-locations"
          aria-selected="false">
    Локации <span class="tab-count">0</span>
  </button>
  <!-- furniture | pets | ui аналогично -->
</nav>
```

### 5.2. Правила переключения

1. По умолчанию активна вкладка `npc` (первая в порядке).
2. Клик по `.tab-btn` с `data-category=X`:
   - Убирает класс `active` со всех `.tab-btn`; добавляет на нажатую.
   - Скрывает все `.assets-panel` (`display: none`).
   - Показывает `div.assets-panel[data-category=X]` (`display: block`).
   - Обновляет `window.location.hash` → `#assets-{X}` (например `#assets-npc`).
3. При загрузке страницы: если `location.hash` соответствует `#assets-{category}` — активировать соответствующий таб.
4. Счётчик `.tab-count` равен `manifest.categories[category].length`.

### 5.3. Таблица hash-якорей

| Вкладка   | `data-category` | Hash URL           |
|-----------|-----------------|--------------------|
| NPC       | `npc`           | `#assets-npc`      |
| Локации   | `locations`     | `#assets-locations`|
| Мебель    | `furniture`     | `#assets-furniture`|
| Питомцы   | `pets`          | `#assets-pets`     |
| UI        | `ui`            | `#assets-ui`       |

---

## 6. JavaScript API

> JS Developer реализует следующие функции согласно спецификации (TASK-FE-052).  
> Аналитик описывает только сигнатуры, контракты входа/выхода и побочные эффекты.

### 6.1. `renderAssetsGallery(manifest)`

```
renderAssetsGallery(manifest: AssetsManifest): void
```

**Вход:** объект `manifest`, соответствующий схеме из раздела 2.  
**Побочные эффекты:**
1. Очищает содержимое каждого `.assets-panel[data-category]`.
2. Для каждой категории:
   - если `manifest.categories[cat].length === 0` — рендерит placeholder (см. раздел 7.1);
   - иначе — рендерит `asset-grid` с карточками `.asset-card` для каждого элемента.
3. Обновляет `.tab-count` в каждом таб-кнопке.
4. Активирует вкладку по `location.hash` или `npc` по умолчанию.

**Не делает:** не загружает `manifest.json` сам (загрузку выполняет вызывающий код).

### 6.2. `switchCategory(categoryId)`

```
switchCategory(categoryId: 'npc' | 'locations' | 'furniture' | 'pets' | 'ui'): void
```

**Вход:** строка-идентификатор категории.  
**Побочные эффекты:**
1. Переключает активный таб (CSS-класс `active`).
2. Скрывает все `.assets-panel`; показывает нужный.
3. Вызывает `history.replaceState(null, '', '#assets-' + categoryId)`.

**Нет возвращаемого значения.**  
**Граничный случай:** если `categoryId` не входит в допустимый список — игнорировать вызов (no-op).

### 6.3. `buildAssetCard(entry, category)`

```
buildAssetCard(entry: AssetEntry, category: string): HTMLElement
```

**Вход:** один элемент из `manifest.categories[category]`.  
**Выход:** готовый DOM-элемент `div.asset-card`.  
**Правила:**
- Если `entry.metaPath === null` или `entry.animations.length === 0`: не рендерить `.animation-list`; добавить класс `.no-meta` на карточку.
- `onerror` на `<img>` должен заменить `src` на `assets/ui/placeholder.png`.

---

## 7. Правила обработки edge-cases

### 7.1. Пустая категория

```html
<div class="assets-empty-placeholder">
  <span class="placeholder-icon">🖼️</span>
  <p>Ассеты ещё не сгенерированы</p>
  <p class="placeholder-hint">Запустите <code>asset-generator</code> для заполнения категории.</p>
</div>
```

- Показывается когда `manifest.categories[cat].length === 0`.
- Таб-счётчик при этом показывает `0`; стиль — opacity 0.5.

### 7.2. Отсутствующий `sprite-atlas.json` (`metaPath === null`)

- Карточка рендерится без блока `.animation-list`.
- В `.asset-details` вместо конкретных значений: `«N/A»`.
- На карточку добавляется CSS-класс `.no-meta`.
- В `.asset-meta` отображается пометка: `⚠️ Метаданные отсутствуют`.

### 7.3. Битое изображение (недоступный PNG)

- `<img onerror>` заменяет `src` на `assets/ui/placeholder.png`.
- На элемент `<img>` добавляется CSS-класс `.broken`.
- Placeholder-иконка имеет размер не менее 64×64 px.
- Путь к placeholder: `assets/ui/placeholder.png` — **обязателен** в выходе `asset-generator`.

### 7.4. Полностью отсутствующий `assets-manifest.json`

- JS-код должен обернуть `fetch('assets-manifest.json')` в try/catch.
- При ошибке: весь раздел `#assets` показывает глобальный placeholder:
  ```
  ❌ Манифест ассетов не найден. Запустите pipeline генерации.
  ```
- Ошибка логируется в `console.error`.

---

## 8. Нефункциональные требования

| Требование              | Значение                                                           |
|-------------------------|--------------------------------------------------------------------|
| Совместимость браузеров | Chrome 100+, Firefox 100+, Safari 15+                             |
| Загрузка изображений    | `loading="lazy"` на всех `<img>` — обязательно                   |
| Доступность             | `role="tablist"`, `role="tab"`, `aria-selected`, `aria-controls`  |
| Без зависимостей        | Только Vanilla JS — никаких фреймворков                           |
| Файл манифеста          | Должен быть доступен по относительному пути `assets-manifest.json` |

---

## 9. Метрики и критерии готовности

> Согласно `system-analyst-skill.md`, раздел 6 (п.6) и раздел 8.

| Критерий                                               | Проверяет |
|--------------------------------------------------------|----------|
| `assets-manifest.json` проходит JSON Schema (раздел 2)| TASK-BE-021 |
| Все 5 категорий рендерятся без JS-ошибок              | TASK-FE-052 |
| Пустая категория показывает placeholder               | TASK-FE-052 |
| Битый PNG заменяется placeholder-иконкой              | TASK-FE-053 |
| Переключение табов обновляет URL hash                 | TASK-FE-052 |
| Deep-link по hash активирует нужную вкладку           | TASK-FE-052 |
| `.asset-card.no-meta` рендерится без `animation-list` | TASK-FE-053 |
| `placeholder.png` присутствует в выходе генератора    | TASK-BE-021 |

---

*Спецификация создана согласно SDD-фазе **Specify** (`system-analyst-skill.md`, раздел 5).  
Следующий шаг: разработчики Java и JS принимают задачи TASK-BE-021, TASK-FE-052, TASK-FE-053.*
