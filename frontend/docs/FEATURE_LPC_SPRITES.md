# Feature Branch: LPC Sprites Integration

## 🎯 Цель

Интеграция бесплатных LPC (Liberated Pixel Cup) спрайтов для быстрого прототипирования персонажей в Life of T v0.1.0.

## ✅ Что сделано

### 1. Новый компонент `LPCCharacter.tsx`
- ✅ Поддержка 64×64 LPC spritesheet
- ✅ Анимации: idle, walk (4 направления)
- ✅ PixiJS v8 для отрисовки
- ✅ Настраиваемый FPS (по умолчанию 12fps)
- ✅ Поддержка эмоций (neutral, happy, sad, tired)

**Файл**: [`frontend/src/components/scene/LPCCharacter.tsx`](../src/components/scene/LPCCharacter.tsx)

### 2. Документация

- ✅ **LPC_INTEGRATION.md** — полное руководство по интеграции
  - Как сгенерировать спрайтшит Татьяны
  - Mapping LPC анимаций на состояния Life of T
  - Лицензирование и атрибуция
  
- ✅ **LPC_QUICKSTART.md** — быстрый старт за 5 минут
  - Шаг за шагом от генерации до запуска
  - Тестирование анимаций
  - Troubleshooting

- ✅ **RoomScreen.example.tsx** — пример использования
  - Click-to-move механика
  - Управление эмоциями
  - Debug panel

### 3. Структура файлов

```
frontend/
├── src/
│   └── components/
│       └── scene/
│           ├── LPCCharacter.tsx          ← НОВЫЙ компонент
│           ├── RoomScreen.example.tsx    ← Пример интеграции
│           ├── Character.tsx             (старый CSS-based)
│           └── RoomScreen.tsx            (пока не изменен)
│
├── public/
│   └── assets/
│       └── characters/
│           └── tatyana/
│               ├── tatyana-lpc-base.png  ← НУЖНО добавить
│               └── CREDITS.txt           ← НУЖНО создать
│
└── docs/
    ├── LPC_INTEGRATION.md      ← Полная документация
    ├── LPC_QUICKSTART.md       ← Быстрый старт
    └── FEATURE_LPC_SPRITES.md  ← Этот файл
```

## 📋 Что нужно сделать вручную

### Шаг 1: Сгенерировать спрайтшит Татьяны

1. Открыть https://sanderfrenken.github.io/Universal-LPC-Spritesheet-Character-Generator/
2. Настроить персонажа согласно `docs/prompts/character-visual-specs.txt`:
   - **Тело**: Light skin (#F5D5B8)
   - **Волосы**: Shoulder wavy, burgundy (#8B1A1A)
   - **Одежда**: Beige sweater (#F5E6D3) + Gray-blue jeans (#6B7280)
   - **Обувь**: White sneakers (#FFFFFF)
   - **Аксессуар**: Gold heart necklace (#FFD700)
3. Скачать PNG → сохранить как `tatyana-lpc-base.png`
4. Поместить в `frontend/public/assets/characters/tatyana/`

**Детали**: См. [`LPC_QUICKSTART.md`](./LPC_QUICKSTART.md)

### Шаг 2: Создать CREDITS.txt

Создать файл `frontend/public/assets/characters/tatyana/CREDITS.txt`:

```txt
Character sprite generated using Universal LPC Spritesheet Character Generator
https://sanderfrenken.github.io/Universal-LPC-Spritesheet-Character-Generator/

License: CC-BY-SA 3.0 / GPL 3.0
Contributors: [See generator credits page]

Layers used:
- Body: Light skin
- Hair: Shoulder wavy burgundy
- Clothes: Beige sweater, gray-blue jeans
- Shoes: White sneakers
- Accessories: Gold heart necklace
```

### Шаг 3: Обновить RoomScreen.tsx

Заменить старый `Character` на новый `LPCCharacter`:

```typescript
// OLD
import { Character } from './Character';

<Character
  position={{ x: 200, y: 150, zIndex: 4 }}
  state="idle"
/>

// NEW
import { LPCCharacter } from './LPCCharacter';

<LPCCharacter
  position={{ x: 200, y: 150, zIndex: 4 }}
  spritesheet="/assets/characters/tatyana/tatyana-lpc-base.png"
  state="idle"
  direction="south"
/>
```

Или скопировать из [`RoomScreen.example.tsx`](../src/components/scene/RoomScreen.example.tsx)

### Шаг 4: Тестирование

```bash
cd frontend
npm run dev
```

Открыть http://localhost:5173 → проверить:
- ✅ Спрайт отображается
- ✅ Idle анимация работает
- ✅ Walk анимация работает (если добавили click-to-move)
- ✅ 4 направления работают (north/south/east/west)

## 🚀 Следующие шаги

### Краткосрочные (v0.1.0)

1. [ ] Сгенерировать и добавить спрайтшит Татьяны
2. [ ] Обновить RoomScreen.tsx с LPCCharacter
3. [ ] Протестировать анимации
4. [ ] Добавить Sam (муж) спрайтшит
5. [ ] Добавить Garfield (кот) — если есть в LPC
6. [ ] Интегрировать с GameState для real-time обновлений

### Среднесрочные (v0.2.0)

1. [ ] Добавить emotion overlay system
   - Happiness particles
   - Tiredness visual effects
   - Stress indicators
2. [ ] Реализовать pathfinding для click-to-move
3. [ ] Добавить collision detection с объектами
4. [ ] Smooth camera follow

### Долгосрочные (v1.0)

1. [ ] Заменить LPC на изометрические спрайты
   - Купить [Isometric Template 64x64](https://pixel-salvaje.itch.io/isometric-character-template-64-pixel-art) ($7)
   - Заказать custom анимации (work, sleep, grooming)
2. [ ] Добавить недостающие анимации:
   - work-computer (25 frames, 6fps)
   - sleep (20 frames, 4fps, 128×64)
   - emotion-joy (25 frames, 10fps)
   - grooming-mirror (25 frames, 8fps)
3. [ ] Перейти на WebP формат для оптимизации

## 📊 Сравнение: CSS vs LPC

| Параметр | CSS Character (старое) | LPC Character (новое) |
|----------|------------------------|----------------------|
| **Визуал** | Простые геометрические фигуры | Полноценный pixel art |
| **Анимации** | Только breathing (idle) | Idle + Walk (4 dir) |
| **Детализация** | Низкая (placeholder) | Средняя (LPC quality) |
| **Одежда** | Захардкожена | Модульная (можно менять) |
| **Размер** | ~200×350 px canvas | 64×64 px sprite (scaled) |
| **Перспектива** | Псевдо-изометрия (CSS transform) | Top-down |
| **Производительность** | PixiJS canvas | PixiJS texture (быстрее) |

## ⚠️ Известные ограничения

### LPC Sprite

1. **Top-down perspective** (не изометрия)
   - Временное решение для прототипа
   - Заменим на изометрию в v1.0

2. **Нет специфичных анимаций**
   - Нет work/sleep/grooming
   - Используем idle + пропсы в качестве workaround

3. **Лицензия CC-BY-SA**
   - Требует атрибуции в игре
   - Нужен CREDITS.txt файл

### Технические

1. **Spritesheet размер**
   - Полный LPC spritesheet ~832×1344 px
   - Может быть большим для мобильных устройств
   - Решение: обрезать неиспользуемые анимации

2. **PixiJS bundle size**
   - PixiJS v8 добавляет ~400KB к bundle
   - Уже используется, так что не проблема

## 📝 Changelog

### 2026-03-02

- ✅ Создана ветка `feature/lpc-sprites-integration`
- ✅ Добавлен `LPCCharacter.tsx` компонент
- ✅ Добавлена документация (LPC_INTEGRATION, LPC_QUICKSTART)
- ✅ Добавлен пример `RoomScreen.example.tsx`
- ⏳ Ожидается: генерация и добавление спрайтшитов

## 🔗 Ссылки

- [LPC Generator](https://sanderfrenken.github.io/Universal-LPC-Spritesheet-Character-Generator/)
- [LPC GitHub](https://github.com/sanderfrenken/Universal-LPC-Spritesheet-Character-Generator)
- [OpenGameArt LPC](https://opengameart.org/content/lpc-character-generator)
- [Документация](./LPC_INTEGRATION.md)
- [Быстрый старт](./LPC_QUICKSTART.md)

---

**Статус ветки**: 🟡 Ready for testing (после добавления спрайтшитов)

**Следующее действие**: Сгенерировать `tatyana-lpc-base.png` согласно [LPC_QUICKSTART.md](./LPC_QUICKSTART.md)