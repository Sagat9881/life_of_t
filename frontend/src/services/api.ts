import { gameApi, ApiError } from '../api/client';
import type { GameStateResponse } from '../types/api';

const getTelegramUserId = (): string => {
  // В Telegram Mini App
  if (window.Telegram?.WebApp?.initDataUnsafe?.user?.id) {
    return window.Telegram.WebApp.initDataUnsafe.user.id.toString();
  }
  // Fallback для demo
  return 'demo-user';
};

/**
 * API wrapper с автоматическим созданием сессии
 */
export const api = {
  /**
   * Получить состояние игры (с автоматическим созданием сессии при 404)
   */
  async getGameState(): Promise<GameStateResponse> {
    const telegramUserId = getTelegramUserId();
    
    try {
      return await gameApi.getState(telegramUserId);
    } catch (error) {
      // Если сессия не найдена - создаём новую
      if (error instanceof ApiError && error.status === 404) {
        console.log('Session not found, creating new session...');
        return await gameApi.startSession({ telegramUserId });
      }
      throw error;
    }
  },

  /**
   * Выполнить действие
   */
  async executeAction(actionCode: string): Promise<GameStateResponse> {
    const telegramUserId = getTelegramUserId();
    return gameApi.executeAction({ telegramUserId, actionCode });
  },

  /**
   * Разрешить конфликт тактикой
   */
  async resolveTactic(conflictId: string, tacticCode: string): Promise<GameStateResponse> {
    const telegramUserId = getTelegramUserId();
    return gameApi.chooseConflictTactic({ telegramUserId, conflictId, tacticCode });
  },

  /**
   * Выбрать вариант в событии
   */
  async selectChoice(eventId: string, choiceCode: string): Promise<GameStateResponse> {
    const telegramUserId = getTelegramUserId();
    return gameApi.chooseEventOption({ telegramUserId, eventId, choiceCode });
  },
};
