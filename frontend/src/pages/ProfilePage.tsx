import { useEffect } from 'react';
import { PlayerPanel } from '../components/game/PlayerPanel';
import { Card } from '../components/shared/Card';
import { StatBar } from '../components/shared/StatBar';
import { useGameStore } from '../store/gameStore';
import { Award, TrendingUp } from 'lucide-react';
import '../styles/pages/ProfilePage.css';

export function ProfilePage() {
  const { player, fetchGameState } = useGameStore();

  useEffect(() => {
    fetchGameState();
  }, [fetchGameState]);

  if (!player) {
    return null;
  }

  const { stats, level } = player;

  return (
    <div className="profile-page">
      <div className="profile-page__header">
        <PlayerPanel player={player} />
      </div>

      <Card variant="elevated" padding="large" className="profile-page__stats-card">
        <div className="profile-page__section-header">
          <TrendingUp size={24} />
          <h2 className="profile-page__section-title">Детальная статистика</h2>
        </div>

        <div className="profile-page__stats-grid">
          <div className="profile-page__stat-item">
            <StatBar
              statKey="energy"
              value={stats.energy}
              showLabel
              showValue
              size="large"
            />
          </div>
          <div className="profile-page__stat-item">
            <StatBar
              statKey="health"
              value={stats.health}
              showLabel
              showValue
              size="large"
            />
          </div>
          <div className="profile-page__stat-item">
            <StatBar
              statKey="stress"
              value={stats.stress}
              showLabel
              showValue
              size="large"
            />
          </div>
          <div className="profile-page__stat-item">
            <StatBar
              statKey="mood"
              value={stats.mood}
              showLabel
              showValue
              size="large"
            />
          </div>
          <div className="profile-page__stat-item">
            <StatBar
              statKey="selfEsteem"
              value={stats.selfEsteem}
              showLabel
              showValue
              size="large"
            />
          </div>
          <div className="profile-page__stat-item">
            <StatBar
              statKey="money"
              value={stats.money}
              showLabel
              showValue
              size="large"
            />
          </div>
        </div>
      </Card>

      <Card variant="elevated" padding="large" className="profile-page__achievements-card">
        <div className="profile-page__section-header">
          <Award size={24} />
          <h2 className="profile-page__section-title">Достижения</h2>
        </div>

        <div className="profile-page__achievement-info">
          <p className="profile-page__level-badge">
            Уровень {level}
          </p>
          <p className="profile-page__achievement-text">
            Достижения будут доступны в следующем обновлении
          </p>
        </div>
      </Card>
    </div>
  );
}
