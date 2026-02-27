import { AlertTriangle, X } from 'lucide-react';
import { TacticCard } from './TacticCard';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import type { Conflict } from '../../types/game';
import '../../styles/components/ConflictResolver.css';

interface ConflictResolverProps {
  conflict: Conflict;
  isLoading?: boolean;
  error?: string | null;
  onSelectTactic?: (tacticCode: string) => void;
  onCancel?: () => void;
  onRetry?: () => void;
}

export function ConflictResolver({
  conflict,
  isLoading = false,
  error = null,
  onSelectTactic,
  onCancel,
  onRetry,
}: ConflictResolverProps) {
  const { description, csp, maxCSP, tactics } = conflict;

  const cspPercentage = (csp / maxCSP) * 100;

  const getCSPColor = (percentage: number) => {
    if (percentage <= 30) return 'var(--color-success)';
    if (percentage <= 60) return 'var(--color-warning)';
    return 'var(--color-danger)';
  };

  if (isLoading) {
    return (
      <div className="conflict-resolver conflict-resolver--loading">
        <LoadingSpinner size="large" text="Обработка конфликта..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className="conflict-resolver conflict-resolver--error">
        <ErrorMessage message={error} {...(onRetry && { onRetry })} />
      </div>
    );
  }

  return (
    <div className="conflict-resolver">
      {onCancel && (
        <button
          onClick={onCancel}
          className="conflict-resolver__close"
          aria-label="Закрыть"
        >
          <X size={24} />
        </button>
      )}

      <div className="conflict-resolver__header">
        <div className="conflict-resolver__icon">
          <AlertTriangle size={32} />
        </div>
        <h2 className="conflict-resolver__title">Конфликт!</h2>
      </div>

      <p className="conflict-resolver__description">{description}</p>

      <div className="conflict-resolver__csp">
        <div className="conflict-resolver__csp-header">
          <span className="conflict-resolver__csp-label">Уровень напряжения (CSP)</span>
          <span className="conflict-resolver__csp-value">
            {csp} / {maxCSP}
          </span>
        </div>
        <div className="conflict-resolver__csp-bar">
          <div
            className="conflict-resolver__csp-fill"
            style={{
              width: `${cspPercentage}%`,
              backgroundColor: getCSPColor(cspPercentage),
            }}
          />
        </div>
      </div>

      <div className="conflict-resolver__tactics">
        <h3 className="conflict-resolver__tactics-title">Выберите тактику:</h3>
        <div className="conflict-resolver__tactics-grid">
          {tactics.map(tactic => (
            <TacticCard
              key={tactic.code}
              tactic={tactic}
              {...(onSelectTactic && { onSelect: onSelectTactic })}
            />
          ))}
        </div>
      </div>

      {tactics.length === 0 && (
        <div className="conflict-resolver__empty">
          <p>Нет доступных тактик</p>
        </div>
      )}
    </div>
  );
}
