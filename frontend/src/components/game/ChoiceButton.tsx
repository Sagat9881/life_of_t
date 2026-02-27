import { ArrowRight } from 'lucide-react';
import { Card } from '../shared/Card';
import { useTelegram } from '../../hooks/useTelegram';
import type { EventChoice } from '../../types/game';
import '../../styles/components/ChoiceButton.css';

interface ChoiceButtonProps {
  choice: EventChoice;
  index: number;
  onSelect?: (choiceCode: string) => void;
}

const CHOICE_COLORS = [
  'var(--color-primary)',
  'var(--color-secondary)',
  'var(--color-accent)',
  'var(--color-info)',
];

export function ChoiceButton({ choice, index, onSelect }: ChoiceButtonProps) {
  const { hapticFeedback } = useTelegram();
  const { code, text, consequences } = choice;

  const color = CHOICE_COLORS[index % CHOICE_COLORS.length];

  const handleClick = () => {
    hapticFeedback?.impactOccurred('medium');
    onSelect?.(code);
  };

  return (
    <Card
      variant="elevated"
      padding="medium"
      onClick={handleClick}
      className="choice-button"
    >
      <div className="choice-button__number" style={{ backgroundColor: color }}>
        {index + 1}
      </div>

      <div className="choice-button__content">
        <p className="choice-button__text">{text}</p>
        
        {consequences && (
          <div className="choice-button__consequences">
            <span className="choice-button__consequences-label">Последствия:</span>
            <span className="choice-button__consequences-text">{consequences}</span>
          </div>
        )}
      </div>

      <div className="choice-button__arrow" style={{ color }}>
        <ArrowRight size={24} />
      </div>
    </Card>
  );
}
