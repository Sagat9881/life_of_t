import { Clock, Zap, Heart, Brain, DollarSign } from 'lucide-react';
import { Card } from '../shared/Card';
import { useTelegram } from '../../hooks/useTelegram';
import type { GameAction } from '../../types/game';
import '../../styles/components/ActionCard.css';

interface ActionCardProps {
  action: GameAction;
  disabled?: boolean;
  onExecute?: (actionCode: string) => void;
}

export function ActionCard({ action, disabled = false, onExecute }: ActionCardProps) {
  const { hapticFeedback } = useTelegram();
  const { code, name, description, timeCost, effects, available } = action;

  const isDisabled = disabled || !available;

  const handleClick = () => {
    if (isDisabled) return;
    
    hapticFeedback?.impactOccurred('medium');
    onExecute?.(code);
  };

  const renderEffect = (key: string, value: number) => {
    if (value === 0) return null;

    const icons: Record<string, JSX.Element> = {
      energy: <Zap size={14} />,
      health: <Heart size={14} />,
      mood: <Brain size={14} />,
      money: <DollarSign size={14} />,
    };

    const isPositive = value > 0;
    const color = isPositive ? 'var(--color-success)' : 'var(--color-danger)';

    return (
      <div key={key} className="action-card__effect" style={{ color }}>
        {icons[key]}
        <span>{isPositive ? '+' : ''}{value}</span>
      </div>
    );
  };

  return (
    <Card
      variant="elevated"
      padding="medium"
      onClick={handleClick}
      className={`action-card ${isDisabled ? 'action-card--disabled' : ''}`}
    >
      <div className="action-card__header">
        <h3 className="action-card__title">{name}</h3>
        <div className="action-card__time">
          <Clock size={16} />
          <span>{timeCost}ч</span>
        </div>
      </div>

      <p className="action-card__description">{description}</p>

      {effects && (
        <div className="action-card__effects">
          {Object.entries(effects).map(([key, value]) => renderEffect(key, value))}
        </div>
      )}

      {!available && (
        <div className="action-card__unavailable">
          Недоступно
        </div>
      )}
    </Card>
  );
}
