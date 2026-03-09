import { AlertTriangle, X } from 'lucide-react';
import { TacticCard } from './TacticCard';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import type { ConflictView } from '../../types/game';
import '../../styles/components/ConflictResolver.css';

interface ConflictResolverProps {
  conflict: ConflictView;
  isLoading?: boolean;
  error?: string | null;
  onSelectTactic?: (conflictId: string, tacticCode: string) => void;
  onCancel?: () => void;
  onRetry?: () => void;
}

export function ConflictResolver({ conflict, isLoading = false, error = null, onSelectTactic, onCancel, onRetry }: ConflictResolverProps) {
  const { id, label, stage, playerCSP, opponentCSP, tactics } = conflict;
  const maxCSP = 100;
  const cspPercentage = (playerCSP / maxCSP) * 100;
  const getCSPColor = (p: number) => p <= 30 ? 'var(--color-success)' : p <= 60 ? 'var(--color-warning)' : 'var(--color-danger)';

  if (isLoading) return <div className="conflict-resolver conflict-resolver--loading"><LoadingSpinner size="large" text="Обработка конфликта..." /></div>;
  if (error) return <div className="conflict-resolver conflict-resolver--error"><ErrorMessage message={error} {...(onRetry && { onRetry })} /></div>;

  return (
    <div className="conflict-resolver">
      {onCancel && <button onClick={onCancel} className="conflict-resolver__close" aria-label="Закрыть"><X size={24} /></button>}
      <div className="conflict-resolver__header">
        <div className="conflict-resolver__icon"><AlertTriangle size={32} /></div>
        <h2 className="conflict-resolver__title">Конфликт!</h2>
      </div>
      <p className="conflict-resolver__description">{label} (стадия: {stage})</p>
      <div className="conflict-resolver__csp">
        <div className="conflict-resolver__csp-header">
          <span className="conflict-resolver__csp-label">Уровень напряжения (CSP)</span>
          <span className="conflict-resolver__csp-value">{playerCSP} vs {opponentCSP}</span>
        </div>
        <div className="conflict-resolver__csp-bar">
          <div className="conflict-resolver__csp-fill" style={{ width: `${cspPercentage}%`, backgroundColor: getCSPColor(cspPercentage) }} />
        </div>
      </div>
      <div className="conflict-resolver__tactics">
        <h3 className="conflict-resolver__tactics-title">Выберите тактику:</h3>
        <div className="conflict-resolver__tactics-grid">
          {tactics.map(tactic => (
            <TacticCard key={tactic.code} tactic={tactic} {...(onSelectTactic && { onSelect: (code: string) => onSelectTactic(id, code) })} />
          ))}
        </div>
      </div>
      {tactics.length === 0 && <div className="conflict-resolver__empty"><p>Нет доступных тактик</p></div>}
    </div>
  );
}
