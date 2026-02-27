import { Heart, Coffee } from 'lucide-react';
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
  dog: '🐶',
};

const PET_TYPE_LABEL: Record<Pet['type'], string> = {
  cat: 'Кот',
  dog: 'Собака',
};

export function PetCard({ pet, onClick }: PetCardProps) {
  const { hapticFeedback } = useTelegram();
  const { id, name, type, mood, hunger, avatarUrl } = pet;

  const getStatusColor = (value: number) => {
    if (value >= 70) return 'var(--color-success)';
    if (value >= 40) return 'var(--color-warning)';
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
          {avatarUrl ? (
            <img src={avatarUrl} alt={name} className="pet-card__avatar-image" />
          ) : (
            <div className="pet-card__avatar-placeholder">
              <span className="pet-card__emoji">{PET_TYPE_EMOJI[type]}</span>
            </div>
          )}
        </div>

        <div className="pet-card__info">
          <h3 className="pet-card__name">{name}</h3>
          <div className="pet-card__type">{PET_TYPE_LABEL[type]}</div>
        </div>
      </div>

      <div className="pet-card__stats">
        <div className="pet-card__stat">
          <div className="pet-card__stat-header">
            <Heart size={14} />
            <span className="pet-card__stat-label">Настроение</span>
          </div>
          <div className="pet-card__stat-bar">
            <div
              className="pet-card__stat-fill"
              style={{
                width: `${mood}%`,
                backgroundColor: getStatusColor(mood),
              }}
            />
          </div>
        </div>

        <div className="pet-card__stat">
          <div className="pet-card__stat-header">
            <Coffee size={14} />
            <span className="pet-card__stat-label">Голод</span>
          </div>
          <div className="pet-card__stat-bar">
            <div
              className="pet-card__stat-fill"
              style={{
                width: `${100 - hunger}%`,
                backgroundColor: getStatusColor(100 - hunger),
              }}
            />
          </div>
        </div>
      </div>
    </Card>
  );
}
