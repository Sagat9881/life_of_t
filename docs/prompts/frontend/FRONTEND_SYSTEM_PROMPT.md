# Frontend — Архитектура и соглашения

## Обзор

Фронтенд «Life of T» — это одностраничное React-приложение (SPA),
реализующее интерфейс игры. Приложение работает как в браузере, так и
как **Telegram Mini App** внутри Telegram Desktop/Mobile.

**Стек:**
- TypeScript 5.x
- React 18 (функциональные компоненты, хуки)
- Zustand (глобальное состояние)
- CSS Modules (изолированные стили)
- Vite (сборщик + dev-сервер)

---

## Структура директорий

```
frontend/src/
├── api/                  — HTTP-клиенты для взаимодействия с backend
│   ├── gameApi.ts        — все запросы к /api/game/*
│   └── types.ts          — TypeScript-типы, отражающие backend View objects
├── components/           — переиспользуемые компоненты
│   ├── ui/               — базовые UI-примитивы (Button, ProgressBar, Modal…)
│   └── game/             — игровые компоненты (ActionCard, StatsPanel…)
├── pages/                — страницы / экраны приложения
│   ├── GamePage/         — основной игровой экран
│   ├── LoadingPage/      — экран загрузки
│   └── EndingPage/       — экран концовки
├── store/                — Zustand stores
│   └── gameStore.ts      — основной стор игры
├── hooks/                — кастомные React-хуки
│   ├── useGameState.ts   — подписка на gameStore
│   └── useTelegram.ts    — интеграция с Telegram WebApp API
├── styles/               — глобальные стили и переменные
│   ├── globals.css       — сброс стилей, шрифты
│   └── variables.css     — CSS-переменные цветов, типографики, spacing
├── utils/                — вспомогательные функции
│   └── formatStats.ts    — форматирование числовых статов
├── App.tsx               — корневой компонент
└── main.tsx              — точка входа, монтирование React
```

---

## Иерархия компонентов

```
App
├── LoadingPage            — пока идёт загрузка первоначального состояния
├── EndingPage             — когда state.ending != null
└── GamePage               — основной игровой экран
    ├── Header
    │   ├── TimeDisplay    — текущий день и время суток
    │   └── LocationBadge  — иконка + название локации
    ├── StatsPanel         — горизонтальная панель характеристик
    │   └── StatBar × 6   — прогресс-бар для каждой характеристики
    ├── CharacterView      — анимированный спрайт персонажа
    ├── ActionList         — список доступных действий
    │   └── ActionCard × N — карточка действия (иконка, название, цена)
    ├── RelationshipsPanel — панель отношений с NPC
    │   └── RelCard × N   — карточка персонажа
    ├── EventModal         — модальное окно случайного события
    │   └── EventOption × N — вариант ответа
    ├── ConflictModal      — модальное окно конфликтной ситуации
    │   └── TacticOption × N — вариант тактики
    └── EndDayButton       — кнопка «Завершить день»
```

---

## Управление состоянием (Zustand)

### gameStore.ts

```typescript
// Типы, отражающие backend View objects
interface GameStateView {
  sessionId: string;
  telegramUserId: string;
  player: PlayerView;
  relationships: RelationshipView[];
  pets: PetView[];
  time: TimeView;
  availableActions: ActionOptionView[];
  activeQuests: QuestView[];
  completedQuestIds: string[];
  activeConflicts: ConflictView[];
  currentEvent: EventView | null;
  ending: EndingView | null;
  lastActionResult: ActionResultView | null;
}

interface GameStore {
  // Состояние
  state: GameStateView | null;
  isLoading: boolean;
  error: string | null;
  pendingAction: string | null;  // id действия, которое выполняется

  // Экшены
  startSession: (telegramUserId: string) => Promise<void>;
  loadState: () => Promise<void>;
  executeAction: (actionId: string) => Promise<void>;
  endDay: () => Promise<void>;
  chooseConflictTactic: (conflictId: string, tacticId: string) => Promise<void>;
  chooseEventOption: (eventId: string, optionId: string) => Promise<void>;

  // UI-хелперы
  clearError: () => void;
}

// Реализация
export const useGameStore = create<GameStore>((set, get) => ({
  state: null,
  isLoading: false,
  error: null,
  pendingAction: null,

  startSession: async (telegramUserId) => {
    set({ isLoading: true, error: null });
    try {
      const state = await gameApi.startSession(telegramUserId);
      set({ state, isLoading: false });
    } catch (err) {
      set({ error: String(err), isLoading: false });
    }
  },

  executeAction: async (actionId) => {
    set({ pendingAction: actionId });
    try {
      const result = await gameApi.executeAction(
        get().state!.telegramUserId, actionId);
      // Перезагружаем полное состояние после действия
      await get().loadState();
    } finally {
      set({ pendingAction: null });
    }
  },

  // ... остальные методы
}));
```

### Принципы работы со стором

- **Один источник правды**: всё состояние игры хранится в `gameStore`.
- **Оптимистичные обновления**: для простых действий можно обновить UI до ответа сервера.
- **Разделение чтения/записи**: компоненты читают через `useGameStore(s => s.state)`,
  вызывают экшены через `useGameStore(s => s.executeAction)`.
- **Нет локального `useState` для игровых данных**: только для UI-состояния компонента
  (открыт ли дропдаун, hover и т.д.).

---

## HTTP API-клиент

```typescript
// api/gameApi.ts
const BASE_URL = import.meta.env.VITE_API_URL ?? '';

export const gameApi = {
  startSession: (telegramUserId: string): Promise<GameStateView> =>
    fetch(`${BASE_URL}/api/game/start`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ telegramUserId }),
    }).then(handleResponse),

  getState: (telegramUserId: string): Promise<GameStateView> =>
    fetch(`${BASE_URL}/api/game/state?userId=${telegramUserId}`)
      .then(handleResponse),

  executeAction: (telegramUserId: string, actionId: string): Promise<ActionResultView> =>
    fetch(`${BASE_URL}/api/game/action`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ telegramUserId, actionId }),
    }).then(handleResponse),

  endDay: (telegramUserId: string): Promise<GameStateView> =>
    fetch(`${BASE_URL}/api/game/end-day`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ telegramUserId }),
    }).then(handleResponse),
};

function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) throw new Error(`HTTP ${res.status}: ${res.statusText}`);
  return res.json();
}
```

---

## Стилизация

### CSS Modules + переменные

Каждый компонент имеет собственный `.module.css` файл:

```
components/game/ActionCard/
├── ActionCard.tsx
└── ActionCard.module.css
```

```css
/* ActionCard.module.css */
.card {
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: var(--spacing-3) var(--spacing-4);
  transition: background 180ms ease;
  cursor: pointer;
}

.card:hover {
  background: var(--color-surface-hover);
}

.card[data-disabled="true"] {
  opacity: 0.5;
  cursor: not-allowed;
}

.energyCost {
  font-size: var(--text-sm);
  color: var(--color-muted);
}
```

```tsx
// ActionCard.tsx
import styles from './ActionCard.module.css';

export function ActionCard({ action, onSelect, isPending }: ActionCardProps) {
  return (
    <button
      className={styles.card}
      data-disabled={!action.available || isPending}
      onClick={() => action.available && onSelect(action.id)}
    >
      <span className={styles.name}>{action.displayName}</span>
      <span className={styles.energyCost}>⚡ {action.energyCost}</span>
    </button>
  );
}
```

### CSS-переменные (variables.css)

```css
:root {
  /* Цвета */
  --color-bg:           #faf7f4;   /* тёплый белый фон */
  --color-surface:      #f0ebe5;   /* поверхность карточки */
  --color-surface-hover:#e8e1d9;
  --color-accent:       #c97c5d;   /* тёплый терракотовый акцент */
  --color-text:         #3d2b1f;   /* основной текст */
  --color-muted:        #8a7568;   /* вспомогательный текст */

  /* Типографика */
  --font-base: 'Inter', system-ui, sans-serif;
  --text-xs:   0.75rem;
  --text-sm:   0.875rem;
  --text-base: 1rem;
  --text-lg:   1.125rem;
  --text-xl:   1.25rem;

  /* Spacing (4px grid) */
  --spacing-1: 4px;
  --spacing-2: 8px;
  --spacing-3: 12px;
  --spacing-4: 16px;
  --spacing-6: 24px;
  --spacing-8: 32px;

  /* Border radius */
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 16px;
}
```

### Соглашения стилизации

- Все цвета — только через CSS-переменные, без хардкода `#hex`.
- Анимации: `transition: <свойство> 180ms cubic-bezier(0.16, 1, 0.3, 1)`.
- Никогда не использовать `!important`.
- Адаптивность: компонент должен корректно отображаться на ширине 375px и выше.

---

## Интеграция с Telegram WebApp

```typescript
// hooks/useTelegram.ts
export function useTelegram() {
  const tg = window.Telegram?.WebApp;

  return {
    telegramUserId: tg?.initDataUnsafe?.user?.id?.toString() ?? 'dev-user',
    isReady: !!tg,
    isExpanded: tg?.isExpanded ?? false,
    expand: () => tg?.expand(),
    close: () => tg?.close(),
    sendData: (data: string) => tg?.sendData(data),
    // Тема Telegram
    colorScheme: tg?.colorScheme ?? 'light',
    themeParams: tg?.themeParams ?? {},
  };
}
```

При разработке локально (без Telegram) `telegramUserId` возвращает `'dev-user'`.

---

## Тестирование

### Стратегия

| Уровень       | Что тестируем                         | Инструменты                   |
|---------------|---------------------------------------|-------------------------------|
| Unit          | Функции, хуки, стор                   | Vitest                        |
| Component     | Рендеринг компонентов                 | Vitest + @testing-library/react |
| E2E           | Пользовательский сценарий             | Playwright (future)           |

### Пример теста компонента

```tsx
import { render, screen, fireEvent } from '@testing-library/react';
import { ActionCard } from '../ActionCard';

const mockAction = {
  id: 'jogging',
  displayName: 'Пробежка',
  energyCost: 25,
  available: true,
};

test('calls onSelect when clicked', () => {
  const onSelect = vi.fn();
  render(<ActionCard action={mockAction} onSelect={onSelect} isPending={false} />);

  fireEvent.click(screen.getByRole('button'));
  expect(onSelect).toHaveBeenCalledWith('jogging');
});

test('does not call onSelect when disabled', () => {
  const onSelect = vi.fn();
  render(<ActionCard action={{ ...mockAction, available: false }} onSelect={onSelect} isPending={false} />);

  fireEvent.click(screen.getByRole('button'));
  expect(onSelect).not.toHaveBeenCalled();
});
```

### Запуск тестов

```bash
# Все тесты
npm test

# Watch-режим
npm run test:watch

# Coverage
npm run test:coverage
```

---

## Доступность (A11y)

- Все кнопки имеют явный `aria-label` или видимый текст.
- Прогресс-бары используют `role="progressbar"` с `aria-valuenow`, `aria-valuemin`, `aria-valuemax`.
- Модальные окна: фокус захватывается и возвращается при закрытии.
- Поддержка навигации с клавиатуры для всех интерактивных элементов.

---

## Производительность

- **Code splitting**: каждый экран — отдельный lazy chunk.
- **Мемоизация**: `React.memo` для тяжёлых list-компонентов.
- **Минимизация ре-рендеров**: Zustand селекторы выбирают только нужные поля стора.
- **Оптимизация изображений**: PNG-спрайты генерируются в нативном разрешении (16px grid).
