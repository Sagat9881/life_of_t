import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { StatBar } from '../../components/shared/StatBar';

describe('StatBar', () => {
  it('renders with correct aria attributes', () => {
    render(<StatBar label="Энергия" value={75} max={100} color="energy" />);
    const bar = screen.getByRole('progressbar');
    expect(bar).toHaveAttribute('aria-valuenow', '75');
    expect(bar).toHaveAttribute('aria-valuemin', '0');
    expect(bar).toHaveAttribute('aria-valuemax', '100');
  });

  it('renders label', () => {
    render(<StatBar label="Здоровье" value={50} max={100} color="health" />);
    expect(screen.getByText('Здоровье')).toBeInTheDocument();
  });

  it('renders value as text', () => {
    render(<StatBar label="Деньги" value={5000} max={10000} color="money" />);
    expect(screen.getByText('5000')).toBeInTheDocument();
  });

  it('handles zero value', () => {
    render(<StatBar label="Стресс" value={0} max={100} color="stress" />);
    const bar = screen.getByRole('progressbar');
    expect(bar).toHaveAttribute('aria-valuenow', '0');
  });

  it('handles max value', () => {
    render(<StatBar label="Настроение" value={100} max={100} color="mood" />);
    const bar = screen.getByRole('progressbar');
    expect(bar).toHaveAttribute('aria-valuenow', '100');
  });
});
