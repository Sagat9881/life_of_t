import { useGameStore } from '@/store/gameStore';
import { StatsPanel } from '@/components/stats/StatsPanel';
import { LoadingSpinner } from '@/components/shared/LoadingSpinner';
import styles from './StatsPage.module.css';

export function StatsPage() {
  const { gameState, isLoading } = useGameStore();

  if (isLoading && !gameState) {
    return (
      <div className={styles.loading}>
        <LoadingSpinner size="lg" text="Загрузка..." />
      </div>
    );
  }

  if (!gameState) {
    return (
      <div className={styles.empty}>
        <p>Начните игру, чтобы увидеть статистику</p>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1 className={styles.title}>📊 Статистика</h1>
        <span className={styles.day}>День {gameState.time.day}</span>
      </div>
      <StatsPanel player={gameState.player} />
    </div>
  );
}
