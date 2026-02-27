# Migration Guide: API Hooks → GameStore

## Изменения

Все хуки из `src/api/hooks/` **удалены**. Их функциональность перенесена в:
- `src/store/gameStore.ts` - Zustand store с методами API
- `src/services/api.ts` - HTTP клиент

## Удаление устаревших файлов

### Windows
```bash
cleanup-deprecated.bat
```

### Linux/Mac
```bash
chmod +x cleanup-deprecated.sh
./cleanup-deprecated.sh
```

### Вручную
```bash
rm src/api/hooks/useActions.ts
rm src/api/hooks/useGameState.ts
```

## Новый подход

### Было (устарело)
```typescript
import { useActions } from '@/api/hooks/useActions';
import { useGameState } from '@/api/hooks/useGameState';

function MyComponent() {
  const { executeAction } = useActions();
  const { gameState } = useGameState();
  // ...
}
```

### Стало (актуально)
```typescript
import { useGameStore } from '@/store/gameStore';

function MyComponent() {
  const {
    player,
    actions,
    executeAction,
    fetchGameState,
  } = useGameStore();

  useEffect(() => {
    fetchGameState();
  }, [fetchGameState]);
  // ...
}
```

## Доступные методы

### State
```typescript
const {
  player,           // Player | null
  time,             // GameTime | null
  actions,          // GameAction[]
  npcs,             // NPC[]
  pets,             // Pet[]
  currentConflict,  // Conflict | null
  currentEvent,     // GameEvent | null
  isLoading,        // boolean
  error,            // string | null
} = useGameStore();
```

### Methods
```typescript
const {
  fetchGameState,   // () => Promise<void>
  executeAction,    // (actionCode: string) => Promise<void>
  selectTactic,     // (tacticCode: string) => Promise<void>
  selectChoice,     // (choiceCode: string) => Promise<void>
  cancelConflict,   // () => void
  cancelEvent,      // () => void
  reset,            // () => void
} = useGameStore();
```

## Примеры использования

### HomePage
```typescript
export function HomePage() {
  const {
    player,
    time,
    actions,
    currentConflict,
    currentEvent,
    executeAction,
    selectTactic,
    selectChoice,
    fetchGameState,
  } = useGameStore();

  useEffect(() => {
    fetchGameState();
  }, [fetchGameState]);

  // Render based on state...
}
```

### RelationshipsPage
```typescript
export function RelationshipsPage() {
  const {
    npcs,
    pets,
    isLoading,
    error,
    fetchGameState,
  } = useGameStore();

  useEffect(() => {
    fetchGameState();
  }, [fetchGameState]);

  // Render relationships...
}
```

---

**Дата миграции**: 27.02.2026
