import { Target, TrendingUp } from 'lucide-react';
import { Card } from '../shared/Card';
import { useTelegram } from '../../hooks/useTelegram';
import type { ConflictTactic } from '../../types/game';
import '../../styles/components/TacticCard.css';

interface TacticCardProps {
  tactic: ConflictTactic;
  onSelect?: (tacticCode: string) => void;
}

export function TacticCard({ tactic, onSelect }: TacticCardProps) {
  const { hapticFeedback } = useTelegram();
  const { code, name, description, successChance } = tactic;

  const getChanceColor = (chance: number) => {
    if (chance >= 70) return 'var(--color-success)';
    if (chance >= 40) return 'var(--color-warning)';
    return 'var(--color-danger)';
  };

  const getChanceLabel = (chance: number) => {
    if (chance >= 80) return 'Высокий шанс';
    if (chance >= 50) return 'Средний шанс';
    return 'Низкий шанс';
  };

  const handleClick = () => {
    hapticFeedback?.impactOccurred('medium');
    onSelect?.(code);
  };

  return (
    <Card
      variant="elevated"
      padding="medium"
      onClick={handleClick}
      className="tactic-card"
    >
      <div className="tactic-card__header">
        <div className="tactic-card__icon">
          <Target size={20} />
        </div>
        <h3 className="tactic-card__title">{name}</h3>
      </div>

      <p className="tactic-card__description">{description}</p>

      <div className="tactic-card__footer">
        <div className="tactic-card__chance">
          <TrendingUp size={16} />
          <span
            className="tactic-card__chance-value"
            style={{ color: getChanceColor(successChance) }}
          >
            {successChance}%
          </span>
        </div>
        <span
          className="tactic-card__chance-label"
          style={{ color: getChanceColor(successChance) }}
        >
          {getChanceLabel(successChance)}
        </span>
      </div>
    </Card>
  );
}
