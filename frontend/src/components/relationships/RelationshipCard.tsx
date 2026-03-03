import { Heart, AlertTriangle } from 'lucide-react';
import type { Relationship } from '@/types/game';
import styles from './RelationshipCard.module.css';

const NPC_LABELS: Record<string, string> = {
  husband: '💑 Муж',
  father: '👨‍👧 Отец',
  friend: '👥 Подруга',
  mother: '👩‍👧 Мама',
  colleague: '🤝 Коллега',
};

const NPC_EMOJIS: Record<string, string> = {
  husband: '💑',
  father: '👨‍👧',
  friend: '👥',
  mother: '👩‍👧',
  colleague: '🤝',
};

interface RelationshipCardProps {
  relationship: Relationship;
}

function RelBar({ label, value, color }: { label: string; value: number; color: string }) {
  const pct = Math.min(100, Math.max(0, value));
  return (
    <div className={styles.relBar}>
      <span className={styles.relBarLabel}>{label}</span>
      <div className={styles.relBarTrack}>
        <div className={styles.relBarFill} style={{ width: `${pct}%`, background: color }} />
      </div>
      <span className={styles.relBarValue}>{Math.round(value)}</span>
    </div>
  );
}

export function RelationshipCard({ relationship }: RelationshipCardProps) {
  const { npcCode, name, closeness, trust, stability, romance, broken } = relationship;
  const emoji = NPC_EMOJIS[npcCode] ?? '👤';
  const label = NPC_LABELS[npcCode] ?? npcCode;

  return (
    <div className={`${styles.card} ${broken ? styles.broken : ''}`}>
      <div className={styles.header}>
        <div className={styles.avatar}>
          <span className={styles.avatarEmoji}>{emoji}</span>
        </div>
        <div className={styles.info}>
          <h3 className={styles.name}>{name}</h3>
          <span className={styles.type}>{label}</span>
        </div>
        {broken && (
          <div className={styles.brokenBadge}>
            <AlertTriangle size={14} />
            <span>Разрыв</span>
          </div>
        )}
        {!broken && closeness > 70 && (
          <Heart size={16} className={styles.heartIcon} fill="currentColor" />
        )}
      </div>

      <div className={styles.bars}>
        <RelBar label="Близость" value={closeness} color="var(--color-primary)" />
        <RelBar label="Доверие" value={trust} color="var(--color-secondary)" />
        <RelBar label="Стабильность" value={stability} color="var(--color-success)" />
        {romance > 0 && (
          <RelBar label="Романтика" value={romance} color="var(--color-danger)" />
        )}
      </div>
    </div>
  );
}

