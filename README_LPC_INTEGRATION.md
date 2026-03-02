# 🎮 Life of T - LPC Sprites Integration

## ✨ Что нового в этой ветке?

Ветка `feature/lpc-sprites-integration` добавляет поддержку **бесплатных LPC (Liberated Pixel Cup) спрайтов** для быстрого прототипирования персонажей.

### ✅ Готово

- **LPCCharacter компонент** — поддержка 64×64 pixel art спрайтов с анимациями
- **Документация** — пошаговые инструкции по генерации и интеграции
- **Примеры** — готовый пример RoomScreen с click-to-move

### 📋 Осталось сделать вручную

1. **Сгенерировать spritesheet Татьяны** (5 минут)
   - Открыть [LPC Generator](https://sanderfrenken.github.io/Universal-LPC-Spritesheet-Character-Generator/)
   - Настроить персонажа согласно specs (burgundy hair, beige sweater, etc.)
   - Скачать PNG → сохранить как `tatyana-lpc-base.png`
   - Поместить в `frontend/public/assets/characters/tatyana/`

2. **Обновить RoomScreen.tsx** (2 минуты)
   - Заменить `<Character />` на `<LPCCharacter />`
   - Указать путь к spritesheet

3. **Тестировать!** 🚀

---

## 📚 Документация

### Быстрый старт (5 минут)

📖 **[LPC_QUICKSTART.md](frontend/docs/LPC_QUICKSTART.md)**  
Пошаговая инструкция: от генерации спрайта до запуска в браузере.

### Полная интеграция

📕 **[LPC_INTEGRATION.md](frontend/docs/LPC_INTEGRATION.md)**  
Детальное руководство:
- Как работает LPC Generator
- Mapping LPC анимаций на состояния Life of T
- Лицензирование и атрибуция
- Ограничения и workarounds

### Обзор ветки

📝 **[FEATURE_LPC_SPRITES.md](frontend/docs/FEATURE_LPC_SPRITES.md)**  
Что сделано, что осталось, roadmap.

---

## 🛠️ Быстрая установка

### Шаг 1: Checkout ветки

```bash
git fetch origin
git checkout feature/lpc-sprites-integration
```

### Шаг 2: Установить зависимости

```bash
cd frontend
npm install
```

### Шаг 3: Сгенерировать спрайтшит

1. Открыть https://sanderfrenken.github.io/Universal-LPC-Spritesheet-Character-Generator/
2. Настроить:
   - Body: Light skin
   - Hair: Shoulder wavy, burgundy (#8B1A1A)
   - Clothes: Beige sweater + gray-blue jeans
   - Shoes: White sneakers
   - Necklace: Gold heart
3. Download PNG → Save as `tatyana-lpc-base.png`
4. Поместить в `frontend/public/assets/characters/tatyana/`

### Шаг 4: Обновить код

Edit `frontend/src/components/scene/RoomScreen.tsx`:

```typescript
import { LPCCharacter } from './LPCCharacter';

// Replace <Character /> with:
<LPCCharacter
  position={{ x: 200, y: 150, zIndex: 4 }}
  spritesheet="/assets/characters/tatyana/tatyana-lpc-base.png"
  state="idle"
  direction="south"
/>
```

Или скопировать из `RoomScreen.example.tsx`

### Шаг 5: Запуск

```bash
npm run dev
```

Открыть http://localhost:5173 → видеть LPC спрайт! 🎉

---

## 💻 Структура файлов

```
frontend/
├── src/
│   └── components/
│       └── scene/
│           ├── LPCCharacter.tsx          ← НОВЫЙ компонент
│           ├── RoomScreen.example.tsx    ← Пример интеграции
│           ├── Character.tsx             (старый CSS-based)
│           └── RoomScreen.tsx            (нужно обновить)
│
├── public/
│   └── assets/
│       └── characters/
│           └── tatyana/
│               ├── tatyana-lpc-base.png  ← ДОБАВИТЬ
│               └── CREDITS.txt           ← СОЗДАТЬ
│
└── docs/
    ├── LPC_INTEGRATION.md      ← Полная документация
    ├── LPC_QUICKSTART.md       ← Быстрый старт
    └── FEATURE_LPC_SPRITES.md  ← Обзор ветки
```

---

## 🎯 Что дальше?

### Краткосрочные (v0.1.0)

- [ ] Добавить Sam (муж) spritesheet
- [ ] Добавить Garfield (кот) — если есть в LPC
- [ ] Интегрировать с GameState для real-time обновлений
- [ ] Добавить emotion overlay system

### Среднесрочные (v0.2.0)

- [ ] Click-to-move с pathfinding
- [ ] Collision detection с объектами
- [ ] Smooth camera follow

### Долгосрочные (v1.0)

- [ ] Заменить LPC на изометрические спрайты
  - Купить [Isometric Template 64x64](https://pixel-salvaje.itch.io/isometric-character-template-64-pixel-art) ($7)
  - Заказать custom анимации (work, sleep, grooming)

---

## ⚠️ Известные ограничения

### Временные (v0.1.0)

1. **Top-down perspective** вместо изометрии
   - LPC спрайты имеют top-down вид (как в Stardew Valley)
   - Это временное решение для прототипа
   - Заменим на изометрию в v1.0

2. **Нет специфичных анимаций**
   - Нет work/sleep/grooming
   - Используем idle + пропсы как workaround

3. **Лицензия CC-BY-SA**
   - Требует атрибуции в игре
   - Нужен CREDITS.txt файл

### Технические

- Spritesheet размер: ~832×1344 px (может быть большим для мобильных)
- PixiJS добавляет ~400KB к bundle (уже используется, не проблема)

---

## 🔗 Ссылки

- [LPC Generator](https://sanderfrenken.github.io/Universal-LPC-Spritesheet-Character-Generator/)
- [LPC GitHub](https://github.com/sanderfrenken/Universal-LPC-Spritesheet-Character-Generator)
- [OpenGameArt LPC](https://opengameart.org/content/lpc-character-generator)
- [Character Visual Specs](docs/prompts/character-visual-specs.txt)

---

## 👏 Credits

- **LPC Contributors**: Hundreds of pixel artists (see [CREDITS](https://github.com/sanderfrenken/Universal-LPC-Spritesheet-Character-Generator/blob/master/CREDITS.TXT))
- **License**: CC-BY-SA 3.0 / GPL 3.0
- **Integration**: Life of T team

---

**Статус**: 🟡 Ready for testing (после добавления спрайтшитов)

**Следующее действие**: Сгенерировать `tatyana-lpc-base.png` согласно [LPC_QUICKSTART.md](frontend/docs/LPC_QUICKSTART.md)