import type { GameState } from '../types/game';

const API_BASE_URL = '/api/v1/game';

// Fallback telegramUserId для demo
const getTelegramUserId = (): string => {
  if (typeof window !== 'undefined' && (window as any).Telegram?.WebApp) {
    const tg = (window as any).Telegram.WebApp;
    return tg.initDataUnsafe?.user?.id?.toString() || 'demo-user';
  }
  return 'demo-user';
};

interface ActionResult {
  player: GameState['player'];
  time: GameState['time'];
  actions: GameState['actions'];
  currentConflict?: GameState['currentConflict'];
  currentEvent?: GameState['currentEvent'];
}

interface TacticResult {
  player: GameState['player'];
}

interface ChoiceResult {
  player: GameState['player'];
}

class ApiClient {
  private sessionStarted = false;

  private async request<T>(endpoint: string, options?: RequestInit): Promise<T> {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options?.headers,
      },
    });

    if (!response.ok) {
      throw new Error(`API Error: ${response.status} ${response.statusText}`);
    }

    return response.json();
  }

  async startSession(): Promise<GameState> {
    const telegramUserId = getTelegramUserId();
    const state = await this.request<GameState>('/session/start', {
      method: 'POST',
      body: JSON.stringify({ telegramUserId }),
    });
    this.sessionStarted = true;
    return state;
  }

  async getGameState(): Promise<GameState> {
    const telegramUserId = getTelegramUserId();
    
    try {
      // Попытка получить существующую сессию
      return await this.request<GameState>(`/state?telegramUserId=${encodeURIComponent(telegramUserId)}`);
    } catch (error) {
      // Если сессии нет (404), создаём новую
      if (error instanceof Error && error.message.includes('404')) {
        return await this.startSession();
      }
      throw error;
    }
  }

  async executeAction(actionCode: string): Promise<ActionResult> {
    const telegramUserId = getTelegramUserId();
    return this.request<ActionResult>('/action', {
      method: 'POST',
      body: JSON.stringify({ 
        telegramUserId,
        actionCode 
      }),
    });
  }

  async resolveTactic(conflictId: string, tacticCode: string): Promise<TacticResult> {
    const telegramUserId = getTelegramUserId();
    return this.request<TacticResult>('/conflict/tactic', {
      method: 'POST',
      body: JSON.stringify({ 
        telegramUserId,
        conflictId, 
        tacticCode 
      }),
    });
  }

  async selectChoice(eventId: string, choiceCode: string): Promise<ChoiceResult> {
    const telegramUserId = getTelegramUserId();
    return this.request<ChoiceResult>('/event-choice', {
      method: 'POST',
      body: JSON.stringify({ 
        telegramUserId,
        eventId, 
        choiceCode 
      }),
    });
  }
}

export const api = new ApiClient();
