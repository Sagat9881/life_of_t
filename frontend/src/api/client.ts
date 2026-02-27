import type {
  GameApi,
  StartSessionRequest,
  ExecuteActionRequest,
  ChooseConflictTacticRequest,
  ChooseEventOptionRequest,
  GameStateResponse,
} from '@/types/api';
import { API_BASE_URL, API_TIMEOUT } from '@/utils/constants';

/**
 * Обработка HTTP ошибок
 */
class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public code?: string
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

/**
 * Базовая функция для HTTP запросов
 */
const request = async <T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> => {
  const url = `${API_BASE_URL}${endpoint}`;

  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), API_TIMEOUT);

  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      signal: controller.signal,
    });

    clearTimeout(timeoutId);

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new ApiError(
        errorData.message || 'Ошибка сервера',
        response.status,
        errorData.code
      );
    }

    return await response.json();
  } catch (error) {
    clearTimeout(timeoutId);

    if (error instanceof ApiError) {
      throw error;
    }

    if (error instanceof Error) {
      if (error.name === 'AbortError') {
        throw new ApiError('Превышено время ожидания', 408);
      }
      throw new ApiError(error.message, 0);
    }

    throw new ApiError('Неизвестная ошибка', 0);
  }
};

/**
 * API клиент для работы с игровым сервером
 */
export const gameApi: GameApi = {
  /**
   * Начать или загрузить игровую сессию
   */
  startSession: async (data: StartSessionRequest): Promise<GameStateResponse> => {
    return request<GameStateResponse>('/api/v1/game/session/start', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  /**
   * Получить текущее состояние игры
   */
  getState: async (telegramUserId: number): Promise<GameStateResponse> => {
    return request<GameStateResponse>(
      `/api/v1/game/state?telegramUserId=${telegramUserId}`,
      {
        method: 'GET',
      }
    );
  },

  /**
   * Выполнить игровое действие
   */
  executeAction: async (data: ExecuteActionRequest): Promise<GameStateResponse> => {
    return request<GameStateResponse>('/api/v1/game/action', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  /**
   * Выбрать тактику разрешения конфликта
   */
  chooseConflictTactic: async (
    data: ChooseConflictTacticRequest
  ): Promise<GameStateResponse> => {
    return request<GameStateResponse>('/api/v1/game/conflict/tactic', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  /**
   * Выбрать вариант ответа на событие
   */
  chooseEventOption: async (
    data: ChooseEventOptionRequest
  ): Promise<GameStateResponse> => {
    return request<GameStateResponse>('/api/v1/game/event-choice', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },
};

export { ApiError };
