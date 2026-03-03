import { useNavigate } from 'react-router-dom';
import { useGameStore } from '@/store/gameStore';
import { Button } from '@/components/shared/Button';
import { StatKeyBar } from '@/components/shared/StatBar';
import styles from './ProfilePage.module.css';

export function ProfilePage() {
  const { gameState, startGame } = useGameStore();
  const navigate = useNavigate();

  if (!gameState) {
    return (
      <div className={styles.empty}>
        <p>Начните игру, чтобы увидеть профиль</p>
        <Button variant="primary" onClick={startGame}>Начать игру</Button>
      </div>
    );
  }

  const { player, time, relationships } = gameState;

  return (
    <div className={styles.page}>
      <div className={styles.hero}>
        <div className={styles.avatarLarge}>👩</div>
        <h1 className={styles.playerName}>{player.name}</h1>
        <p className={styles.subtitle}>День {time.day} игры</p>
      </div>

      <div className={styles.section}>
        <h2 className={styles.sectionTitle}>Показатели</h2>
        <div className={styles.statsList}>
          <StatKeyBar statKey="energy" value={player.stats.energy} showLabel />
          <StatKeyBar statKey="health" value={player.stats.health} showLabel />
          <StatKeyBar statKey="mood" value={player.stats.mood} showLabel />
          <StatKeyBar statKey="stress" value={player.stats.stress} showLabel />
          <StatKeyBar statKey="selfEsteem" value={player.stats.selfEsteem} showLabel />
        </div>
      </div>

      <div className={styles.section}>
        <h2 className={styles.sectionTitle}>Достижения</h2>
        <div className={styles.tags}>
          {time.day >= 7 && <span className={styles.tag}>🏆 Неделя пройдена</span>}
          {player.stats.energy > 80 && <span className={styles.tag}>⚡ Энергичная</span>}
          {player.stats.mood > 80 && <span className={styles.tag}>😊 Счастливая</span>}
          {player.stats.money > 5000 && <span className={styles.tag}>💰 Богатая</span>}
          {relationships.filter(r => r.closeness > 80).length > 0 && (
            <span className={styles.tag}>💕 Любимая</span>
          )}
          {relationships.filter(r => !r.broken).length === relationships.length && relationships.length > 0 && (
            <span className={styles.tag}>🤝 Миротворец</span>
          )}
        </div>
        {time.day < 3 && player.stats.mood < 70 && (
          <p className={styles.noAchievements}>Достижения появятся со временем</p>
        )}
      </div>

      <div className={styles.actions}>
        <Button variant="primary" fullWidth onClick={() => navigate('/room')}>
          Вернуться в комнату
        </Button>
      </div>
    </div>
  );
}
