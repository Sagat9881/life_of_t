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

  async getGameState(): Promise<GameState> {
    const telegramUserId = getTelegramUserId();
    return this.request<GameState>(`/state?telegramUserId=${encodeURIComponent(telegramUserId)}`);
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
