/**
 * Sidebar — right panel with stats, quests, and relationships.
 * Pixel-art styled, matches original vanilla demo.
 */
import type { Player, NPC, GameTime } from '../../types/game';
import './Sidebar.css';

interface SidebarProps {
  readonly player: Player;
  readonly npcs: readonly NPC[];
  readonly gameTime: GameTime;
}

/** Stat display config */
const STAT_CONFIG = [
  { key: 'energy', label: 'Энергия', color: '#4caf50' },
  { key: 'health', label: 'Здоровье', color: '#f44336' },
  { key: 'stress', label: 'Стресс', color: '#ff9800' },
  { key: 'mood', label: 'Настроение', color: '#2196f3' },
  { key: 'money', label: 'Деньги', color: '#f093fb' },
  { key: 'selfEsteem', label: 'Самооценка', color: '#ffe66d' },
] as const;

/** Relationship hearts (filled ♥ / empty ♡) */
function Hearts({ value, max = 10 }: { value: number; max?: number }) {
  const filled = Math.round((value / 100) * max);
  return (
    <span className="sb-hearts">
      {'♥'.repeat(Math.min(filled, max))}
      {'♡'.repeat(Math.max(max - filled, 0))}
    </span>
  );
}

export function Sidebar({ player, npcs }: SidebarProps) {
  const stats = player.stats;

  return (
    <aside className="sb">
      {/* ── STATS ── */}
      <section className="sb-section">
        <h2 className="sb-section__title">
          {player.name || 'ТАНЯ'} — СТАТЫ
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

      {/* ── QUESTS ── */}
      <section className="sb-section">
        <h2 className="sb-section__title">АКТИВНЫЕ КВЕСТЫ</h2>
        <div className="sb-quests">
          <div className="sb-quest">
            <span className="sb-quest__tag sb-quest__tag--care">ЗАБОТА О СЕБЕ</span>
            <div className="sb-quest__name">Найди себя</div>
            <ul className="sb-quest__tasks">
              <li>Поспать не менее 7 часов (0/1)</li>
              <li>Сходить в спортзал (0/1)</li>
            </ul>
          </div>
          <div className="sb-quest">
            <span className="sb-quest__tag sb-quest__tag--family">СЕМЬЯ</span>
            <div className="sb-quest__name">Семейный вечер</div>
            <ul className="sb-quest__tasks">
              <li>Поговорить с мужем (0/1)</li>
              <li>Позвонить папе (0/1)</li>
            </ul>
          </div>
        </div>
      </section>

      {/* ── RELATIONSHIPS ── */}
      <section className="sb-section">
        <h2 className="sb-section__title">ОТНОШЕНИЯ</h2>
        <div className="sb-relationships">
          {npcs.length > 0 ? (
            npcs.map((npc) => (
              <div className="sb-rel" key={npc.id}>
                <span className="sb-rel__name">{npc.name}</span>
                <Hearts value={npc.relationship} />
              </div>
            ))
          ) : (
            <>
              <div className="sb-rel">
                <span className="sb-rel__name">Александр</span>
                <Hearts value={70} />
              </div>
              <div className="sb-rel">
                <span className="sb-rel__name">Папа</span>
                <Hearts value={50} />
              </div>
              <div className="sb-rel">
                <span className="sb-rel__name">Сэм 🐾</span>
                <Hearts value={90} />
              </div>
            </>
          )}
        </div>
      </section>
    </aside>
  );
}
