import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { RoomPage } from '../../pages/RoomPage';
import * as gameStoreModule from '../../store/gameStore';
import type { GameStateView } from '../../types/game';

const mockState: GameStateView = {
  sessionId: 'session-1',
  telegramUserId: 'user-1',
  player: {
    id: 'player-1',
    name: 'Татьяна',
    stats: {
      energy: 75,
      health: 80,
      stress: 30,
      mood: 65,
      money: 5000,
      selfEsteem: 70,
    },
    job: { title: 'Менеджер', salary: 50000, level: 1 },
    location: 'home',
    inventory: [],
    tags: {},
    skills: {},
  },
  relationships: [],
  pets: [],
  time: { day: 1, hour: 9, period: 'morning' },
  availableActions: [
    { id: 'jogging', displayName: 'Пробежка', description: 'Утренняя пробежка', energyCost: 25, available: true, unavailableReason: null },
  ],
  activeQuests: [],
  completedQuestIds: [],
  activeConflicts: [],
  currentEvent: null,
  ending: null,
  lastActionResult: null,
};

describe('RoomPage', () => {
  beforeEach(() => {
    vi.spyOn(gameStoreModule, 'useGameStore').mockImplementation((selector: any) =>
      selector({
        state: mockState,
        isLoading: false,
        error: null,
        pendingAction: null,
        executeAction: vi.fn(),
        endDay: vi.fn(),
        chooseConflictTactic: vi.fn(),
        chooseEventOption: vi.fn(),
      })
    );
  });

  it('renders room scene', () => {
    render(<MemoryRouter><RoomPage /></MemoryRouter>);
    expect(screen.getByTestId('room-scene')).toBeInTheDocument();
  });

  it('renders available actions', () => {
    render(<MemoryRouter><RoomPage /></MemoryRouter>);
    expect(screen.getByText('Пробежка')).toBeInTheDocument();
  });
});
