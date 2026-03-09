import { Target } from 'lucide-react';
import { Card } from '../shared/Card';
import { useTelegram } from '../../hooks/useTelegram';
import type { TacticOptionView } from '../../types/game';
import '../../styles/components/TacticCard.css';

interface TacticCardProps {
  tactic: TacticOptionView;
  onSelect?: (tacticCode: string) => void;
}

export function TacticCard({ tactic, onSelect }: TacticCardProps) {
  const { hapticFeedback } = useTelegram();
  const { code, label, description } = tactic;

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
        <h3 className="tactic-card__title">{label}</h3>
      </div>

      <p className="tactic-card__description">{description}</p>
    </Card>
  );
}
