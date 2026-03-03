import { create } from 'zustand';
import { api } from '@/services/api';
import type { GameState } from '@/types/game';

interface Toast {
  id: string;
  message: string;
  type: 'success' | 'error' | 'info';
}

interface GameStore {
  sessionId: string | null;
  gameState: GameState | null;
  isLoading: boolean;
  isActionLoading: boolean;
  error: string | null;
  toast: Toast | null;

  // Actions
  startGame: () => Promise<void>;
  refreshState: () => Promise<void>;
  executeAction: (actionType: string) => Promise<void>;
  endDay: () => Promise<void>;
  chooseConflictTactic: (tacticId: string) => Promise<void>;
  chooseEventOption: (eventId: string, optionId: string) => Promise<void>;
  clearError: () => void;
  showToast: (message: string, type?: Toast['type']) => void;
  clearToast: () => void;
}

export const useGameStore = create<GameStore>((set, get) => ({
  sessionId: null,
  gameState: null,
  isLoading: false,
  isActionLoading: false,
  error: null,
  toast: null,

  startGame: async () => {
    set({ isLoading: true, error: null });
    try {
      const { sessionId } = await api.startGame();
      const gameState = await api.getState(sessionId);
      set({ sessionId, gameState, isLoading: false });
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Не удалось запустить игру';
      set({ error: message, isLoading: false });
    }
  },

  refreshState: async () => {
    const { sessionId } = get();
    if (!sessionId) return;
    try {
      const gameState = await api.getState(sessionId);
      set({ gameState });
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Ошибка обновления';
      set({ error: message });
    }
  },

  executeAction: async (actionType: string) => {
    const { sessionId } = get();
    if (!sessionId) return;
    set({ isActionLoading: true, error: null });
    try {
      const gameState = await api.executeAction({ sessionId, actionType });
      set({ gameState, isActionLoading: false });

      if (gameState.lastActionResult?.message) {
        get().showToast(gameState.lastActionResult.message, 'success');
      }

      // Check for ending
      if (gameState.ending) {
        // Navigation handled by the page component watching the store
      }
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Действие не выполнено';
      set({ error: message, isActionLoading: false });
      get().showToast(message, 'error');
    }
  },

  endDay: async () => {
    const { sessionId } = get();
    if (!sessionId) return;
    set({ isActionLoading: true, error: null });
    try {
      const gameState = await api.endDay(sessionId);
      set({ gameState, isActionLoading: false });
      get().showToast(`День ${gameState.time.day} завершён`, 'success');
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Ошибка завершения дня';
      set({ error: message, isActionLoading: false });
    }
  },

  chooseConflictTactic: async (tacticId: string) => {
    const { sessionId } = get();
    if (!sessionId) return;
    set({ isActionLoading: true, error: null });
    try {
      const gameState = await api.chooseConflictTactic({ sessionId, tacticId });
      set({ gameState, isActionLoading: false });
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Ошибка выбора тактики';
      set({ error: message, isActionLoading: false });
    }
  },

  chooseEventOption: async (eventId: string, optionId: string) => {
    const { sessionId } = get();
    if (!sessionId) return;
    set({ isActionLoading: true, error: null });
    try {
      const gameState = await api.chooseEventOption({ sessionId, eventId, optionId });
      set({ gameState, isActionLoading: false });
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Ошибка выбора варианта';
      set({ error: message, isActionLoading: false });
    }
  },

  clearError: () => set({ error: null }),

  showToast: (message: string, type: Toast['type'] = 'info') => {
    const id = Date.now().toString();
    set({ toast: { id, message, type } });
    setTimeout(() => {
      const { toast } = get();
      if (toast?.id === id) {
        set({ toast: null });
      }
    }, 3000);
  },

  clearToast: () => set({ toast: null }),
}));
