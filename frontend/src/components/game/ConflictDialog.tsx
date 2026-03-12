import type { ConflictView } from '../../types/game';

interface ConflictDialogProps {
  readonly conflict: ConflictView;
  readonly isLoading: boolean;
  readonly onSelectTactic: (tacticCode: string) => void;
  readonly onRetreat: () => void;
}

export function ConflictDialog({ conflict, isLoading, onSelectTactic, onRetreat }: ConflictDialogProps) {
  const totalCSP = conflict.playerCSP + conflict.opponentCSP;
  const playerPct = totalCSP > 0 ? Math.round((conflict.playerCSP / totalCSP) * 100) : 50;
  const opponentPct = 100 - playerPct;

  return (
    <div className="gs-dialog-overlay">
      <div className="gs-dialog gs-conflict" onClick={(e) => e.stopPropagation()}>
        <div className="gs-conflict__header">
          <div className="gs-dialog__title">{conflict.label}</div>
          <div className="gs-conflict__stage">Стадия: {conflict.stage}</div>
        </div>

        <div className="gs-conflict__csp">
          <div className="gs-conflict__csp-label">
            <span className="gs-conflict__csp-you">Ты</span>
            <span className="gs-conflict__csp-vs">vs</span>
            <span className="gs-conflict__csp-opponent">Оппонент</span>
          </div>
          <div className="gs-conflict__csp-bar">
            <div
              className="gs-conflict__csp-player"
              style={{ width: `${playerPct}%` }}
            />
            <div
              className="gs-conflict__csp-enemy"
              style={{ width: `${opponentPct}%` }}
            />
          </div>
          <div className="gs-conflict__csp-values">
            <span>{conflict.playerCSP}</span>
            <span>{conflict.opponentCSP}</span>
          </div>
        </div>

        <div className="gs-conflict__tactics-label">Тактики</div>
        <div className="gs-conflict__tactics">
          {conflict.tactics.map((tactic) => (
            <button
              key={tactic.code}
              className="gs-conflict__tactic"
              disabled={isLoading}
              onClick={() => onSelectTactic(tactic.code)}
            >
              <span className="gs-conflict__tactic-name">{tactic.label}</span>
              <span className="gs-conflict__tactic-desc">{tactic.description}</span>
            </button>
          ))}
        </div>

        <div className="gs-dialog__buttons">
          <button
            className="gs-dialog__btn gs-dialog__btn--cancel"
            disabled={isLoading}
            onClick={onRetreat}
          >
            Отступить
          </button>
        </div>
      </div>
    </div>
  );
}
