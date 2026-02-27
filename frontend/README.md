# Life of T - Frontend

## Описание

React + TypeScript + Vite frontend для Telegram Mini App игры "Life of T".

## Технологии

- **React 18** - UI фреймворк
- **TypeScript** - строгая типизация
- **Vite** - сборщик
- **Zustand** - state management
- **CSS Modules** - стили
- **Lucide React** - иконки

## Структура

```
src/
├── components/
│   ├── shared/      # Переиспользуемые UI
│   ├── game/        # Игровые компоненты
│   └── layout/      # Лейаут
├── pages/           # Страницы
├── store/           # Zustand store
├── services/        # API клиенты
├── types/           # TypeScript типы
├── utils/           # Утилиты
├── hooks/           # Custom hooks
├── styles/          # CSS файлы
├── App.tsx          # Главное приложение
└── main.tsx         # Entry point
```

## Страницы

- **HomePage** - главная (действия, конфликты, события)
- **RelationshipsPage** - отношения с NPCs и питомцами
- **ProfilePage** - профиль игрока

## Routing

Используется простой state-based routing через `useState<NavItem>`.

## Команды

### Development
```bash
npm install
npm run dev
```

### Production Build
```bash
npm run build
```

### Type Check
```bash
npm run type-check
```

### Lint
```bash
npm run lint
```

## Интеграция с Backend

Frontend собирается через Maven Plugin и упаковывается в Spring Boot JAR.

### Сборка через Maven
```bash
cd ..
mvn clean install -DskipTests
```

## Telegram WebApp

### Инициализация
Приложение автоматически инициализирует Telegram WebApp API в `main.tsx`.

### Haptic Feedback
Используйте `useTelegram` hook:

```typescript
const { hapticFeedback } = useTelegram();
hapticFeedback?.impactOccurred('medium');
```

## Стилизация

### CSS Variables
Все цвета определены в `styles/variables.css`:

```css
--color-primary: #FF6B9D
--color-secondary: #C8E6C9
--color-accent: #FFE66D
```

### Шрифты
- **Comfortaa** - заголовки
- **System fonts** - текст

## Компоненты

### Shared
- Button, Card, StatBar
- LoadingSpinner, ErrorMessage

### Game
- PlayerPanel
- ActionCard, ActionList
- NPCCard, PetCard, RelationshipList
- TacticCard, ConflictResolver
- ChoiceButton, EventChoice

### Layout
- AppLayout, BottomNav

## State Management

### gameStore (Zustand)

```typescript
const {
  player,
  actions,
  npcs,
  pets,
  currentConflict,
  currentEvent,
  executeAction,
  selectTactic,
  selectChoice,
} = useGameStore();
```

## API Integration

API клиент в `services/api.ts`:

```typescript
import { api } from '../services/api';

const state = await api.getGameState();
const result = await api.executeAction(actionCode);
```

## Browser Support

- Chrome/Edge 90+
- Safari 14+
- Firefox 88+
- Telegram WebApp

## License

Private project

---

**Version**: 0.1.0-SNAPSHOT  
**Date**: 27.02.2026
