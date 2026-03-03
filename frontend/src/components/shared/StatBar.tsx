import styles from './StatBar.module.css';

type StatKey = 'energy' | 'health' | 'stress' | 'mood' | 'money' | 'selfEsteem';

interface StatBarProps {
  label: string;
  value: number;
  maxValue?: number;
  color: string;
  icon?: string;
  showValue?: boolean;
  size?: 'sm' | 'md';
}

const STAT_LABELS: Record<StatKey, string> = {
  energy: 'Энергия',
  health: 'Здоровье',
  stress: 'Стресс',
  mood: 'Настроение',
  money: 'Деньги',
  selfEsteem: 'Самооценка',
};

const STAT_COLORS: Record<StatKey, string> = {
  energy: 'var(--stat-energy)',
  health: 'var(--stat-health)',
  stress: 'var(--stat-stress)',
  mood: 'var(--stat-mood)',
  money: 'var(--stat-money)',
  selfEsteem: 'var(--color-primary)',
};

const STAT_ICONS: Record<StatKey, string> = {
  energy: '⚡',
  health: '❤️',
  stress: '😤',
  mood: '😊',
  money: '₽',
  selfEsteem: '⭐',
};

export function StatBar({ label, value, maxValue = 100, color, icon, showValue = true, size = 'md' }: StatBarProps) {
  const pct = Math.min(100, Math.max(0, (value / maxValue) * 100));
  return (
    <div className={`${styles.statBar} ${styles[size]}`}>
      {icon && <span className={styles.icon}>{icon}</span>}
      <div className={styles.info}>
        <div className={styles.labelRow}>
          <span className={styles.label}>{label}</span>
          {showValue && <span className={styles.value}>{Math.round(value)}</span>}
        </div>
        <div className={styles.track}>
          <div
            className={styles.fill}
            style={{ width: `${pct}%`, background: color }}
          />
        </div>
      </div>
    </div>
  );
}

// Named convenience export for stat-key based usage
interface StatKeyBarProps {
  statKey: StatKey;
  value: number;
  maxValue?: number;
  showLabel?: boolean;
  size?: 'sm' | 'md';
}

export function StatKeyBar({ statKey, value, maxValue = 100, showLabel = true, size = 'md' }: StatKeyBarProps) {
  return (
    <StatBar
      label={showLabel ? STAT_LABELS[statKey] : ''}
      value={value}
      maxValue={maxValue}
      color={STAT_COLORS[statKey]}
      icon={STAT_ICONS[statKey]}
      showValue={showLabel}
      size={size}
    />
  );
}

export { STAT_LABELS, STAT_COLORS, STAT_ICONS };
export type { StatKey };
