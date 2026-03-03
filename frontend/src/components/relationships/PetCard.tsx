import { Heart, AlertCircle } from 'lucide-react';
import type { Pet } from '@/types/game';
import styles from './PetCard.module.css';

const PET_EMOJIS: Record<string, string> = {
  cat: '🐱',
  dog: '🐕',
};

const PET_LABELS: Record<string, string> = {
  cat: 'Кот',
  dog: 'Цвергпинчер',
};

interface PetCardProps {
  pet: Pet;
}

function PetStatBar({ value, color, label }: { value: number; color: string; label: string }) {
  const pct = Math.min(100, Math.max(0, value));
  return (
    <div className={styles.petBar}>
      <span className={styles.petBarLabel}>{label}</span>
      <div className={styles.petBarTrack}>
        <div className={styles.petBarFill} style={{ width: `${pct}%`, background: color }} />
      </div>
      <span className={styles.petBarValue}>{Math.round(value)}</span>
    </div>
  );
}

export function PetCard({ pet }: PetCardProps) {
  const { name, type, satiety, attention, health, mood } = pet;

  const isHungry = satiety < 30;
  const needsAttention = attention < 30;

  return (
    <div className={`${styles.card} ${(isHungry || needsAttention) ? styles.needsAttention : ''}`}>
      <div className={styles.header}>
        <div className={styles.avatar}>
          <span className={styles.avatarEmoji}>{PET_EMOJIS[type] ?? '🐾'}</span>
        </div>
        <div className={styles.info}>
          <h3 className={styles.name}>{name}</h3>
          <span className={styles.type}>{PET_LABELS[type] ?? type}</span>
        </div>
        <div className={styles.alerts}>
          {isHungry && (
            <span className={styles.alertBadge} title="Голодный">
              <AlertCircle size={14} />
            </span>
          )}
          {needsAttention && (
            <span className={styles.attentionBadge} title="Хочет внимания">
              <Heart size={14} />
            </span>
          )}
        </div>
      </div>

      <div className={styles.stats}>
        <PetStatBar label="Настроение" value={mood} color="var(--stat-mood)" />
        <PetStatBar label="Здоровье" value={health} color="var(--stat-health)" />
        <PetStatBar label="Сытость" value={satiety} color="var(--color-success)" />
        <PetStatBar label="Внимание" value={attention} color="var(--color-primary)" />
      </div>
    </div>
  );
}
