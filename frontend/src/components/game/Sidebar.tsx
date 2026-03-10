/**
 * Sidebar — right panel: stats, actions from backend, quests, relationships.
 * No hardcoded quest/action data — everything from backend GameStateView.
 */
import type { Player, GameTime, ActionOption, QuestView, RelationshipView } from '../../types/game';
import './Sidebar.css';

interface SidebarProps {
  readonly player: Player;
  readonly gameTime: GameTime;
  readonly availableActions: readonly ActionOption[];
  readonly activeQuests: readonly QuestView[];
  readonly relationships: readonly RelationshipView[];
  readonly onActionClick: (action: ActionOption) => void;
}

const STAT_CONFIG = [
  { key: 'energy', label: 'Энергия', color: '#4caf50' },
  { key: 'health', label: 'Здоровье', color: '#f44336' },
  { key: 'stress', label: 'Стресс', color: '#ff9800' },
  { key: 'mood', label: 'Настроение', color: '#2196f3' },
  { key: 'money', label: 'Деньги', color: '#f093fb' },
  { key: 'selfEsteem', label: 'Самооценка', color: '#ffe66d' },
] as const;

function Hearts({ value, max = 10 }: { value: number; max?: number }) {
  const filled = Math.round((value / 100) * max);
  return (
    <span className="sb-hearts">
      {'♥'.repeat(Math.min(filled, max))}
      {'♡'.repeat(Math.max(max - filled, 0))}
    </span>
  );
}

export function Sidebar({ player, availableActions, activeQuests, relationships, onActionClick }: SidebarProps) {
  const stats = player.stats;

  return (
    <aside className="sb">
      {/* ── STATS ── */}
      <section className="sb-section">
        <h2 className="sb-section__title">
          {player.name || 'ТАТЬЯНА'} — СТАТЫ
        </h2>
        <div className="sb-stats">
          {STAT_CONFIG.map(({ key, label, color }) => {
            const value = stats[key as keyof typeof stats] ?? 0;
            return (
              <div className="sb-stat" key={key}>
                <span className="sb-stat__label">{label}</span>
                <div className="sb-stat__bar">
                  <div
                    className="sb-stat__fill"
                    style={{
                      width: `${Math.min(Math.max(value, 0), 100)}%`,
                      backgroundColor: color,
                    }}
                  />
                </div>
                <span className="sb-stat__value">{value}</span>
              </div>
            );
          })}
        </div>
      </section>

      {/* ── ACTIONS (from backend) ── */}
      <section className="sb-section">
        <h2 className="sb-section__title">ДЕЙСТВИЯ</h2>
        <div className="sb-actions">
          {availableActions.length > 0 ? (
            availableActions.map((action) => (
              <button
                key={action.code}
                className={`sb-action ${action.isAvailable ? '' : 'sb-action--disabled'}`}
                onClick={() => action.isAvailable && onActionClick(action)}
                disabled={!action.isAvailable}
                title={action.unavailableReason ?? action.description}
              >
                <span className="sb-action__label">{action.label}</span>
                <span className="sb-action__cost">⏱{action.estimatedTimeCost}ч</span>
              </button>
            ))
          ) : (
            <div className="sb-empty">Нет доступных действий</div>
          )}
        </div>
      </section>

      {/* ── QUESTS (from backend) ── */}
      <section className="sb-section">
        <h2 className="sb-section__title">КВЕСТЫ</h2>
        <div className="sb-quests">
          {activeQuests.length > 0 ? (
            activeQuests.map((quest) => (
              <div className="sb-quest" key={quest.id}>
                <div className="sb-quest__name">{quest.label}</div>
                <div className="sb-quest__progress">
                  <div className="sb-quest__bar">
                    <div
                      className="sb-quest__fill"
                      style={{ width: `${quest.progressPercent}%` }}
                    />
                  </div>
                  <span className="sb-quest__pct">{quest.progressPercent}%</span>
                </div>
              </div>
            ))
          ) : (
            <div className="sb-empty">Нет активных квестов</div>
          )}
        </div>
      </section>

      {/* ── RELATIONSHIPS ── */}
      <section className="sb-section">
        <h2 className="sb-section__title">ОТНОШЕНИЯ</h2>
        <div className="sb-relationships">
          {relationships.length > 0 ? (
            relationships.map((rel) => (
              <div className="sb-rel" key={rel.npcId}>
                <span className="sb-rel__name">{rel.name || rel.npcId}</span>
                <Hearts value={rel.closeness} />
              </div>
            ))
          ) : (
            <div className="sb-empty">Нет отношений</div>
          )}
        </div>
      </section>
    </aside>
  );
}
