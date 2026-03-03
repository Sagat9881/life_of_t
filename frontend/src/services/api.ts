import type {
  GameState,
  ExecuteActionRequest,
  ConflictTacticRequest,
  EventOptionRequest,
} from '@/types/game';

const API_BASE = '/api';
const TIMEOUT = 15000;

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

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), TIMEOUT);

  try {
    const response = await fetch(`${API_BASE}${endpoint}`, {
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
        errorData.message || `HTTP ${response.status}`,
        response.status,
        errorData.code
      );
    }

    return await response.json();
  } catch (error) {
    clearTimeout(timeoutId);
    if (error instanceof ApiError) throw error;
    if (error instanceof Error) {
      if (error.name === 'AbortError') throw new ApiError('Превышено время ожидания', 408);
      throw new ApiError(error.message, 0);
    }
    throw new ApiError('Неизвестная ошибка', 0);
  }
}

export const api = {
  startGame: (): Promise<{ sessionId: string }> =>
    request<{ sessionId: string }>('/game/start', { method: 'POST' }),

  getState: (sessionId: string): Promise<GameState> =>
    request<GameState>(`/game/state?sessionId=${sessionId}`),

  executeAction: (data: ExecuteActionRequest): Promise<GameState> =>
    request<GameState>('/game/action', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  endDay: (sessionId: string): Promise<GameState> =>
    request<GameState>('/game/end-day', {
      method: 'POST',
      body: JSON.stringify({ sessionId }),
    }),

  chooseConflictTactic: (data: ConflictTacticRequest): Promise<GameState> =>
    request<GameState>('/game/conflict/tactic', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  chooseEventOption: (data: EventOptionRequest): Promise<GameState> =>
    request<GameState>('/game/event/option', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  getAvailableActions: (sessionId: string): Promise<GameState['availableActions']> =>
    request<GameState['availableActions']>(`/game/actions?sessionId=${sessionId}`),
};

export { ApiError };
