import { Heart, AlertCircle } from 'lucide-react';
import { Card } from '../shared/Card';
import { useTelegram } from '../../hooks/useTelegram';
import type { PetView } from '../../types/game';
import '../../styles/components/PetCard.css';

interface PetCardProps {
  pet: PetView;
  onClick?: (petId: string) => void;
}

const PET_CODE_EMOJI: Record<string, string> = {
  cat: '🐱', dog: '🐕', garfield: '🐱', klop: '🐱',
  lada: '🐱', persi: '🐱', thelma: '🐱', duke: '🐕', voland: '🐕',
};

export function PetCard({ pet, onClick }: PetCardProps) {
  const { hapticFeedback } = useTelegram();
  const { petId, petCode, name, mood, satiety } = pet;
  const emoji = PET_CODE_EMOJI[petCode] ?? '🐾';

  const getColor = (value: number) => {
    if (value >= 70) return 'var(--color-success)';
    if (value >= 40) return 'var(--color-warning)';
    return 'var(--color-danger)';
  };

  const handleClick = () => { hapticFeedback?.impactOccurred('light'); onClick?.(petId); };

  return (
    <Card variant="elevated" padding="medium" onClick={handleClick} className="pet-card">
      <div className="pet-card__header">
        <div className="pet-card__avatar"><span className="pet-card__emoji">{emoji}</span></div>
        <div className="pet-card__info">
          <h3 className="pet-card__name">{name}</h3>
          <span className="pet-card__type">{petCode}</span>
        </div>
      </div>
      <div className="pet-card__stats">
        <div className="pet-card__stat">
          <div className="pet-card__stat-header"><Heart size={16} className="pet-card__stat-icon" /><span className="pet-card__stat-label">Настроение</span></div>
          <div className="pet-card__stat-bar"><div className="pet-card__stat-fill" style={{ width: `${mood}%`, backgroundColor: getColor(mood) }} /></div>
          <span className="pet-card__stat-value">{mood}%</span>
        </div>
        <div className="pet-card__stat">
          <div className="pet-card__stat-header"><AlertCircle size={16} className="pet-card__stat-icon" /><span className="pet-card__stat-label">Сытость</span></div>
          <div className="pet-card__stat-bar"><div className="pet-card__stat-fill" style={{ width: `${satiety}%`, backgroundColor: getColor(satiety) }} /></div>
          <span className="pet-card__stat-value">{satiety}%</span>
        </div>
      </div>
    </Card>
  );
}
