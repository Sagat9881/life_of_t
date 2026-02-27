import type { GameState } from '../types/game';

const API_BASE_URL = '/api/game';

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
    return this.request<GameState>('/state');
  }

  async executeAction(actionCode: string): Promise<ActionResult> {
    return this.request<ActionResult>('/action', {
      method: 'POST',
      body: JSON.stringify({ actionCode }),
    });
  }

  async resolveTactic(conflictId: string, tacticCode: string): Promise<TacticResult> {
    return this.request<TacticResult>('/conflict/tactic', {
      method: 'POST',
      body: JSON.stringify({ conflictId, tacticCode }),
    });
  }

  async selectChoice(eventId: string, choiceCode: string): Promise<ChoiceResult> {
    return this.request<ChoiceResult>('/event/choice', {
      method: 'POST',
      body: JSON.stringify({ eventId, choiceCode }),
    });
  }
}

export const api = new ApiClient();
