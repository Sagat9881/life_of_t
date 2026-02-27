import { Heart, AlertCircle } from 'lucide-react';
import { Card } from '../shared/Card';
import { useTelegram } from '../../hooks/useTelegram';
import type { Pet } from '../../types/game';
import '../../styles/components/PetCard.css';

interface PetCardProps {
  pet: Pet;
  onClick?: (petId: string) => void;
}

const PET_TYPE_EMOJI: Record<Pet['type'], string> = {
  cat: '🐱',
  dog: '🐕',
};

const PET_TYPE_LABELS: Record<Pet['type'], string> = {
  cat: 'Кот',
  dog: 'Цвергпинчер',
};

export function PetCard({ pet, onClick }: PetCardProps) {
  const { hapticFeedback } = useTelegram();
  const { id, name, type, mood, hunger } = pet;

  const getMoodColor = (value: number) => {
    if (value >= 70) return 'var(--color-success)';
    if (value >= 40) return 'var(--color-warning)';
    return 'var(--color-danger)';
  };

  const getHungerColor = (value: number) => {
    if (value <= 30) return 'var(--color-success)';
    if (value <= 60) return 'var(--color-warning)';
    return 'var(--color-danger)';
  };

  const handleClick = () => {
    hapticFeedback?.impactOccurred('light');
    onClick?.(id);
  };

  return (
    <Card
      variant="elevated"
      padding="medium"
      onClick={handleClick}
      className="pet-card"
    >
      <div className="pet-card__header">
        <div className="pet-card__avatar">
          <span className="pet-card__emoji">{PET_TYPE_EMOJI[type]}</span>
        </div>
        <div className="pet-card__info">
          <h3 className="pet-card__name">{name}</h3>
          <span className="pet-card__type">{PET_TYPE_LABELS[type]}</span>
        </div>
      </div>

      <div className="pet-card__stats">
        <div className="pet-card__stat">
          <div className="pet-card__stat-header">
            <Heart size={16} className="pet-card__stat-icon" />
            <span className="pet-card__stat-label">Настроение</span>
          </div>
          <div className="pet-card__stat-bar">
            <div
              className="pet-card__stat-fill"
              style={{
                width: `${mood}%`,
                backgroundColor: getMoodColor(mood),
              }}
            />
          </div>
          <span className="pet-card__stat-value">{mood}%</span>
        </div>

        <div className="pet-card__stat">
          <div className="pet-card__stat-header">
            <AlertCircle size={16} className="pet-card__stat-icon" />
            <span className="pet-card__stat-label">Голод</span>
          </div>
          <div className="pet-card__stat-bar">
            <div
              className="pet-card__stat-fill"
              style={{
                width: `${hunger}%`,
                backgroundColor: getHungerColor(hunger),
              }}
            />
          </div>
          <span className="pet-card__stat-value">{hunger}%</span>
        </div>
      </div>
    </Card>
  );
}
