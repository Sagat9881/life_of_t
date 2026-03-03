import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { ActionCard } from '../../components/actions/ActionCard';
import type { ActionOption } from '../../types/game';

const mockAction: ActionOption = {
  id: 'jogging',
  displayName: 'Пробежка',
  description: 'Утренняя пробежка в парке',
  energyCost: 25,
  available: true,
  unavailableReason: null,
};

describe('ActionCard', () => {
  it('renders action name and energy cost', () => {
    render(<ActionCard action={mockAction} onSelect={vi.fn()} isPending={false} />);
    expect(screen.getByText('Пробежка')).toBeInTheDocument();
    expect(screen.getByText('25')).toBeInTheDocument();
  });

  it('calls onSelect with action id when clicked', () => {
    const onSelect = vi.fn();
    render(<ActionCard action={mockAction} onSelect={onSelect} isPending={false} />);
    fireEvent.click(screen.getByRole('button'));
    expect(onSelect).toHaveBeenCalledWith('jogging');
  });

  it('does not call onSelect when action is unavailable', () => {
    const onSelect = vi.fn();
    render(<ActionCard action={{ ...mockAction, available: false }} onSelect={onSelect} isPending={false} />);
    fireEvent.click(screen.getByRole('button'));
    expect(onSelect).not.toHaveBeenCalled();
  });

  it('shows unavailable reason when action is disabled', () => {
    render(
      <ActionCard
        action={{ ...mockAction, available: false, unavailableReason: 'Нет энергии' }}
        onSelect={vi.fn()}
        isPending={false}
      />
    );
    expect(screen.getByText('Нет энергии')).toBeInTheDocument();
  });
});
