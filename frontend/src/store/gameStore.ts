import { create } from 'zustand';
import { api } from '../services/api';
import type { GameState, Player, NPC, Pet, GameAction, Conflict, GameEvent, GameTime } from '../types/game';

interface GameStore {
  // State
  player: Player | null;
  time: GameTime | null;
  actions: GameAction[];
  npcs: NPC[];
  pets: Pet[];
  currentConflict: Conflict | null;
  currentEvent: GameEvent | null;
  isLoading: boolean;
  error: string | null;

  // Actions
  fetchGameState: () => Promise<void>;
  executeAction: (actionCode: string) => Promise<void>;
  selectTactic: (tacticCode: string) => Promise<void>;
  selectChoice: (choiceCode: string) => Promise<void>;
  cancelConflict: () => void;
  cancelEvent: () => void;
  reset: () => void;
}

const initialState = {
  player: null,
  time: null,
  actions: [],
  npcs: [],
  pets: [],
  currentConflict: null,
  currentEvent: null,
  isLoading: false,
  error: null,
};

/**
 * Zustand store для игрового состояния
 */
export const useGameStore = create<GameStore>((set, get) => ({
  ...initialState,

  fetchGameState: async () => {
    set({ isLoading: true, error: null });
    try {
      const state = await api.getGameState();
      set({
        player: state.player,
        time: state.time,
        actions: state.actions,
        npcs: state.npcs,
        pets: state.pets,
        currentConflict: state.currentConflict,
        currentEvent: state.currentEvent,
        isLoading: false,
      });
    } catch (error) {
      set({
        error: error instanceof Error ? error.message : 'Не удалось загрузить данные',
        isLoading: false,
      });
    }
  },

  executeAction: async (actionCode: string) => {
    set({ isLoading: true, error: null });
    try {
      const result = await api.executeAction(actionCode);
      set({
        player: result.player,
        time: result.time,
        actions: result.actions,
        currentConflict: result.currentConflict,
        currentEvent: result.currentEvent,
        isLoading: false,
      });
    } catch (error) {
      set({
        error: error instanceof Error ? error.message : 'Не удалось выполнить действие',
        isLoading: false,
      });
    }
  },

  selectTactic: async (tacticCode: string) => {
    const { currentConflict } = get();
    if (!currentConflict) return;

    set({ isLoading: true, error: null });
    try {
      const result = await api.resolveTactic(currentConflict.id, tacticCode);
      set({
        player: result.player,
        currentConflict: null, // Конфликт разрешён
        isLoading: false,
      });
    } catch (error) {
      set({
        error: error instanceof Error ? error.message : 'Не удалось разрешить конфликт',
        isLoading: false,
      });
    }
  },

  selectChoice: async (choiceCode: string) => {
    const { currentEvent } = get();
    if (!currentEvent) return;

    set({ isLoading: true, error: null });
    try {
      const result = await api.selectChoice(currentEvent.id, choiceCode);
      set({
        player: result.player,
        currentEvent: null, // Событие завершено
        isLoading: false,
      });
    } catch (error) {
      set({
        error: error instanceof Error ? error.message : 'Не удалось выбрать вариант',
        isLoading: false,
      });
    }
  },

  cancelConflict: () => {
    set({ currentConflict: null });
  },

  cancelEvent: () => {
    set({ currentEvent: null });
  },

  reset: () => set(initialState),
}));
