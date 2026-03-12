/**
 * Sidebar — right panel: stats, actions from backend, quests, relationships.
 * No hardcoded quest/action data — everything from backend GameStateView.
 */
import type {
  Player,
  ActionOption,
  QuestView,
  RelationshipView,
  PetView,
  NpcActivityView,
  DomainEventView,
} from '../../types/game';
import './Sidebar.css';

interface SidebarProps {
  readonly player: Player;
  readonly availableActions: readonly ActionOption[];
  readonly activeQuests: readonly QuestView[];
  readonly relationships: readonly RelationshipView[];
  readonly pets: readonly PetView[];
  readonly npcActivities: readonly NpcActivityView[];
  readonly domainEvents: readonly DomainEventView[];
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

const PET_STAT_CONFIG = [
  { key: 'satiety', label: 'Сытость', color: '#4caf50' },
  { key: 'attention', label: 'Внимание', color: '#2196f3' },
  { key: 'health', label: 'Здоровье', color: '#f44336' },
  { key: 'mood', label: 'Настроение', color: '#f093fb' },
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

export function Sidebar({
  player,
  availableActions,
  activeQuests,
  relationships,
  pets,
  npcActivities,
  domainEvents,
  onActionClick,
}: SidebarProps) {
  const stats = player.stats;
  const latestDomainEvents = domainEvents.slice(-5).reverse();

  return (
    <aside className="sb">
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

      <section className="sb-section">
        <h2 className="sb-section__title">ПИТОМЦЫ</h2>
        <div className="sb-pets">
          {pets.length > 0 ? (
            pets.map((pet) => (
              <div className="sb-pet" key={pet.petId}>
                <div className="sb-pet__name">{pet.name}</div>
                <div className="sb-pet__stats">
                  {PET_STAT_CONFIG.map(({ key, label, color }) => {
                    const value = pet[key as keyof PetView];
                    const numericValue = typeof value === 'number' ? value : 0;
                    return (
                      <div className="sb-pet-stat" key={key}>
                        <span className="sb-pet-stat__label">{label}</span>
                        <div className="sb-pet-stat__bar">
                          <div
                            className="sb-pet-stat__fill"
                            style={{
                              width: `${Math.min(Math.max(numericValue, 0), 100)}%`,
                              backgroundColor: color,
                            }}
                          />
                        </div>
                        <span className="sb-pet-stat__value">{numericValue}</span>
                      </div>
                    );
                  })}
                </div>
              </div>
            ))
          ) : (
            <div className="sb-empty">Нет питомцев</div>
          )}
        </div>
      </section>

      <section className="sb-section">
        <h2 className="sb-section__title">АКТИВНОСТИ NPC</h2>
        <div className="sb-npc-list">
          {npcActivities.length > 0 ? (
            npcActivities.map((activity) => (
              <div className="sb-npc" key={activity.npcId}>
                <div className="sb-npc__top">
                  <span className="sb-npc__name">{activity.displayName}</span>
                  <span
                    className={`sb-npc__status ${activity.isAvailable ? 'sb-npc__status--available' : 'sb-npc__status--busy'}`}
                  >
                    {activity.isAvailable ? 'Доступен' : 'Занят'}
                  </span>
                </div>
                <div className="sb-npc__meta">{activity.activityId}</div>
                <div className="sb-npc__mood">{activity.moodSummary}</div>
              </div>
            ))
          ) : (
            <div className="sb-empty">Нет активностей</div>
          )}
        </div>
      </section>

      <section className="sb-section">
        <h2 className="sb-section__title">СОБЫТИЯ МИРА</h2>
        <div className="sb-events">
          {latestDomainEvents.length > 0 ? (
            latestDomainEvents.map((event, index) => (
              <div className="sb-event" key={`${event.timestamp}-${event.eventType}-${index}`}>
                <div className="sb-event__type">{event.eventType}</div>
                <div className="sb-event__time">{event.timestamp}</div>
              </div>
            ))
          ) : (
            <div className="sb-empty">Нет событий</div>
          )}
        </div>
      </section>
    </aside>
  );
}
