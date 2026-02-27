import { create } from 'zustand';
import type { GameState } from '@/types/game';

interface GameStore {
  // State
  gameState: GameState | null;
  isLoading: boolean;
  error: string | null;
  telegramUserId: number | null;

  // Actions
  setGameState: (state: GameState) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  setTelegramUserId: (id: number) => void;
  reset: () => void;
}

const initialState = {
  gameState: null,
  isLoading: false,
  error: null,
  telegramUserId: null,
};

/**
 * Zustand store для игрового состояния
 */
export const useGameStore = create<GameStore>((set) => ({
  ...initialState,

  setGameState: (state) => set({ gameState: state, error: null }),

  setLoading: (loading) => set({ isLoading: loading }),

  setError: (error) => set({ error, isLoading: false }),

  setTelegramUserId: (id) => set({ telegramUserId: id }),

  reset: () => set(initialState),
}));

// Селекторы для оптимизации ре-рендеров
export const selectGameState = (state: GameStore) => state.gameState;
export const selectIsLoading = (state: GameStore) => state.isLoading;
export const selectError = (state: GameStore) => state.error;
export const selectPlayer = (state: GameStore) => state.gameState?.player;
export const selectStats = (state: GameStore) => state.gameState?.player.stats;
export const selectRelationships = (state: GameStore) => state.gameState?.relationships;
export const selectPets = (state: GameStore) => state.gameState?.pets;
export const selectTime = (state: GameStore) => state.gameState?.time;
export const selectAvailableActions = (state: GameStore) => state.gameState?.availableActions;
export const selectActiveConflicts = (state: GameStore) => state.gameState?.activeConflicts;
export const selectCurrentEvent = (state: GameStore) => state.gameState?.currentEvent;
