import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { HUD } from '../../components/hud/HUD';
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
  availableActions: [],
  activeQuests: [],
  completedQuestIds: [],
  activeConflicts: [],
  currentEvent: null,
  ending: null,
  lastActionResult: null,
};

describe('HUD', () => {
  it('renders player name', () => {
    render(<HUD state={mockState} onEndDay={vi.fn()} />);
    expect(screen.getByText('Татьяна')).toBeInTheDocument();
  });

  it('renders day and time info', () => {
    render(<HUD state={mockState} onEndDay={vi.fn()} />);
    expect(screen.getByText(/День 1/)).toBeInTheDocument();
  });

  it('renders all stat bars', () => {
    render(<HUD state={mockState} onEndDay={vi.fn()} />);
    expect(screen.getByLabelText(/Энергия/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Здоровье/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Стресс/i)).toBeInTheDocument();
  });
});
