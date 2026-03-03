import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { HUD } from '@/components/hud/HUD';
import { useGameStore } from '@/store/gameStore';
import type { GameState } from '@/types/game';

// Mock the store
vi.mock('@/store/gameStore');

const mockGameState: GameState = {
  sessionId: 'test-123',
  player: {
    name: 'Таня',
    stats: { energy: 75, health: 60, stress: 40, mood: 80, money: 1500, selfEsteem: 70 },
    job: { satisfaction: 60, burnoutRisk: 25 },
    location: 'home',
  },
  relationships: [],
  pets: [],
  time: { day: 3, hour: 14 },
  availableActions: [],
};

function setupStore(overrides?: Partial<ReturnType<typeof useGameStore>>) {
  (useGameStore as unknown as ReturnType<typeof vi.fn>).mockReturnValue({
    gameState: mockGameState,
    isActionLoading: false,
    endDay: vi.fn(),
    ...overrides,
  });
}

describe('HUD', () => {
  it('renders loading state when no game state', () => {
    setupStore({ gameState: null });
    render(<HUD />);
    expect(screen.getByText(/загрузка/i)).toBeDefined();
  });

  it('displays money with rouble symbol', () => {
    setupStore();
    render(<HUD />);
    // Should show formatted money with ₽
    expect(screen.getByText('₽')).toBeDefined();
    // toLocaleString('ru-RU') may use a non-breaking space (U+00A0) or a regular space depending on environment
    expect(screen.getByText(/1[\s\u00a0]500/)).toBeDefined();
  });

  it('displays current day', () => {
    setupStore();
    render(<HUD />);
    expect(screen.getByText(/день 3/i)).toBeDefined();
  });

  it('displays formatted time', () => {
    setupStore();
    render(<HUD />);
    expect(screen.getByText('14:00')).toBeDefined();
  });

  it('shows end day button', () => {
    setupStore();
    render(<HUD />);
    const btn = screen.getByRole('button', { name: /спать/i });
    expect(btn).toBeDefined();
  });

  it('disables end day button when action is loading', () => {
    setupStore({ isActionLoading: true });
    render(<HUD />);
    const btn = screen.getByRole('button');
    expect(btn).toHaveProperty('disabled', true);
  });
});
