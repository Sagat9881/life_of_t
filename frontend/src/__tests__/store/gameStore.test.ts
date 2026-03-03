import { describe, it, expect, vi, beforeEach } from 'vitest';
import { act } from 'react';
import { useGameStore } from '../../store/gameStore';

// Mock the API
vi.mock('../../services/api', () => ({
  gameApi: {
    startSession: vi.fn(),
    getState: vi.fn(),
    executeAction: vi.fn(),
    endDay: vi.fn(),
    chooseConflictTactic: vi.fn(),
    chooseEventOption: vi.fn(),
  },
}));

import { gameApi } from '../../services/api';

const mockState = {
  sessionId: 'session-1',
  telegramUserId: 'user-1',
  player: {
    id: 'player-1',
    name: 'Татьяна',
    stats: { energy: 75, health: 80, stress: 30, mood: 65, money: 5000, selfEsteem: 70 },
    job: { title: 'Менеджер', salary: 50000, level: 1 },
    location: 'home',
    inventory: [], tags: {}, skills: {},
  },
  relationships: [], pets: [],
  time: { day: 1, hour: 9, period: 'morning' },
  availableActions: [], activeQuests: [], completedQuestIds: [],
  activeConflicts: [], currentEvent: null, ending: null, lastActionResult: null,
};

describe('gameStore', () => {
  beforeEach(() => {
    useGameStore.setState({ state: null, isLoading: false, error: null, pendingAction: null });
    vi.clearAllMocks();
  });

  it('startSession sets state on success', async () => {
    vi.mocked(gameApi.startSession).mockResolvedValueOnce(mockState as any);

    await act(async () => {
      await useGameStore.getState().startSession('user-1');
    });

    expect(useGameStore.getState().state).toEqual(mockState);
    expect(useGameStore.getState().isLoading).toBe(false);
  });

  it('startSession sets error on failure', async () => {
    vi.mocked(gameApi.startSession).mockRejectedValueOnce(new Error('Network error'));

    await act(async () => {
      await useGameStore.getState().startSession('user-1');
    });

    expect(useGameStore.getState().error).toBeTruthy();
    expect(useGameStore.getState().isLoading).toBe(false);
  });

  it('executeAction calls api and reloads state', async () => {
    useGameStore.setState({ state: mockState as any });
    vi.mocked(gameApi.executeAction).mockResolvedValueOnce({} as any);
    vi.mocked(gameApi.getState).mockResolvedValueOnce(mockState as any);

    await act(async () => {
      await useGameStore.getState().executeAction('jogging');
    });

    expect(gameApi.executeAction).toHaveBeenCalledWith('user-1', 'jogging');
  });
});
