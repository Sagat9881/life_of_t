import { useMemo } from 'react';
import type { Stats } from '@/types/game';
import { STAT_CONFIG } from '@/config/stats';
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
  maxValue,
  showLabel = true,
  showValue = true,
  size = 'medium',
  animated = true,
}: StatBarProps) => {
  const config = STAT_CONFIG[statKey];
  const effectiveMax = maxValue ?? config.max;

  const percentage = useMemo(() => {
    return Math.min(Math.max((value / effectiveMax) * 100, 0), 100);
  }, [value, effectiveMax]);

  const color = useMemo(() => {
    if (statKey === 'money') {
      return 'var(--color-accent)';
    }
    if (config.inverted) {
      return getStressColor(value, effectiveMax);
    }
    return getStatColor(value, effectiveMax);
  }, [statKey, value, effectiveMax, config.inverted]);

  const label = config.name;

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
