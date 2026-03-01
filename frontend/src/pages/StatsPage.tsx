import { useGameStore } from '../store/gameStore';
import { StatBar } from '../components/shared/StatBar';
import { BottomNav } from '../components/layout/BottomNav/BottomNav';
import type { StatKey } from '../types/game';
import './StatsPage.css';

const STAT_LABELS: Record<StatKey, string> = {
  energy: 'Энергия',
  health: 'Здоровье',
  stress: 'Стресс',
  mood: 'Настроение',
  money: 'Деньги',
  selfEsteem: 'Самооценка',
};

export function StatsPage() {
  const { player } = useGameStore();

  if (!player) {
    return (
      <div className="stats-page">
        <div className="stats-page__loading">Загрузка...</div>
        <BottomNav current="stats" />
      </div>
    );
  }

  const stats = player.stats;
  const statKeys = Object.keys(stats) as StatKey[];

  return (
    <div className="stats-page">
      <div className="stats-page__content">
        <header className="stats-page__header">
          <h1 className="stats-page__title">📊 Статистика</h1>
          <p className="stats-page__subtitle">
            Уровень {player.level} • {player.name}
          </p>
        </header>

        <div className="stats-page__stats">
          {statKeys.map((key) => (
            <div key={key} className="stats-page__stat-item">
              <div className="stats-page__stat-header">
                <span className="stats-page__stat-label">{STAT_LABELS[key]}</span>
                <span className="stats-page__stat-value">
                  {key === 'money' ? `${stats[key]} ₽` : `${stats[key]}%`}
                </span>
              </div>
              <StatBar statKey={key} value={stats[key]} />
            </div>
          ))}
        </div>

        {player.job && (
          <div className="stats-page__job">
            <h2 className="stats-page__section-title">💼 Работа</h2>
            <div className="stats-page__job-card">
              <p className="stats-page__job-title">{player.job.title}</p>
              <p className="stats-page__job-company">{player.job.company}</p>
              <p className="stats-page__job-salary">Зарплата: {player.job.salary} ₽</p>
            </div>
          </div>
        )}
      </div>

      <BottomNav current="stats" />
    </div>
  );
}
