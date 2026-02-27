import { STAT_CONFIG } from '../../config/stats';
import type { StatKey } from '../../types/game';
import '../../styles/components/StatBar.css';

interface StatBarProps {
  statKey: StatKey;
  value: number;
  maxValue?: number;
  showLabel?: boolean;
  showValue?: boolean;
  size?: 'small' | 'medium' | 'large';
  animated?: boolean;
}

export function StatBar({
  statKey,
  value,
  maxValue,
  showLabel = false,
  showValue = true,
  size = 'medium',
  animated = true,
}: StatBarProps) {
  const config = STAT_CONFIG[statKey];
  const max = maxValue ?? config.max;
  const percentage = Math.min(Math.max((value / max) * 100, 0), 100);
  
  const getColor = () => {
    if (config.inverted) {
      if (percentage >= 70) return 'var(--color-danger)';
      if (percentage >= 40) return 'var(--color-warning)';
      return 'var(--color-success)';
    }
    
    if (percentage >= 70) return 'var(--color-success)';
    if (percentage >= 40) return 'var(--color-warning)';
    return 'var(--color-danger)';
  };

  const formattedValue = statKey === 'money' 
    ? `${value.toLocaleString('ru-RU')} ₽`
    : `${Math.round(value)}`;

  return (
    <div className={`stat-bar stat-bar--${size}`}>
      {showLabel && (
        <div className="stat-bar__header">
          <div className="stat-bar__label">
            <span className="stat-bar__icon">{config.icon}</span>
            <span className="stat-bar__name">{config.name}</span>
          </div>
          {showValue && (
            <span className="stat-bar__value">
              {formattedValue}
              {statKey !== 'money' && <span className="stat-bar__max">/{max}</span>}
            </span>
          )}
        </div>
      )}
      
      <div className="stat-bar__track">
        <div
          className={`stat-bar__fill ${animated ? 'stat-bar__fill--animated' : ''}`}
          style={{
            width: `${percentage}%`,
            backgroundColor: getColor(),
          }}
        />
      </div>
    </div>
  );
}
