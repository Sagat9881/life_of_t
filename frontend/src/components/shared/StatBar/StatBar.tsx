import { useMemo } from 'react';
import type { Stats } from '@/types/game';
import { STAT_NAMES } from '@/utils/constants';
import { getStatColor, getStressColor } from '@/utils/formatters';
import styles from './StatBar.module.css';

interface StatBarProps {
  statKey: keyof Stats;
  value: number;
  maxValue?: number;
  showLabel?: boolean;
  showValue?: boolean;
  size?: 'small' | 'medium' | 'large';
  animated?: boolean;
}

export const StatBar = ({
  statKey,
  value,
  maxValue = 100,
  showLabel = true,
  showValue = true,
  size = 'medium',
  animated = true,
}: StatBarProps) => {
  const percentage = useMemo(() => {
    return Math.min(Math.max((value / maxValue) * 100, 0), 100);
  }, [value, maxValue]);

  const color = useMemo(() => {
    if (statKey === 'stress') {
      return getStressColor(value);
    }
    if (statKey === 'money') {
      return 'var(--color-accent)';
    }
    return getStatColor(value);
  }, [statKey, value]);

  const label = STAT_NAMES[statKey];

  const barClassNames = [
    styles.bar,
    styles[size],
    animated ? styles.animated : '',
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div className={styles.container}>
      {showLabel && (
        <div className={styles.header}>
          <span className={styles.label}>{label}</span>
          {showValue && (
            <span className={styles.value}>
              {statKey === 'money' ? `${value} ₽` : value}
            </span>
          )}
        </div>
      )}
      <div className={barClassNames}>
        <div
          className={styles.fill}
          style={{
            width: `${percentage}%`,
            backgroundColor: color,
          }}
        />
      </div>
    </div>
  );
};
