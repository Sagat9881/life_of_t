import { useEffect } from 'react';
import { useGameStore } from '@/store/gameStore';
import { RoomScene } from '@/components/room/RoomScene';
import { EventDialog } from '@/components/events/EventDialog';
import { ConflictDialog } from '@/components/events/ConflictDialog';
import { LoadingSpinner } from '@/components/shared/LoadingSpinner';
import styles from './RoomPage.module.css';

export function RoomPage() {
  const {
    gameState,
    isLoading,
    isActionLoading,
    error,
    startGame,
    executeAction,
    chooseEventOption,
    chooseConflictTactic,
  } = useGameStore();

  // Start game on first load
  useEffect(() => {
    if (!gameState && !isLoading) {
      startGame();
    }
  }, [gameState, isLoading, startGame]);

  if (isLoading) {
    return (
      <div className={styles.loadingScreen}>
        <div className={styles.loadingContent}>
          <div className={styles.loadingTitle}>Life of T</div>
          <LoadingSpinner size="lg" text="Загрузка игры..." />
        </div>
      </div>
    );
  }

  if (error && !gameState) {
    return (
      <div className={styles.errorScreen}>
        <div className={styles.errorContent}>
          <p className={styles.errorText}>⚠️ {error}</p>
          <button className={styles.retryBtn} onClick={startGame}>
            Попробовать снова
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      {gameState ? (
        <RoomScene onActionSelect={executeAction} />
      ) : (
        <div className={styles.emptyRoom} />
      )}

      {/* Event dialog overlay */}
      {gameState?.activeEvent && (
        <EventDialog
          event={gameState.activeEvent}
          isLoading={isActionLoading}
          onChoose={(optionId) =>
            chooseEventOption(gameState.activeEvent!.id, optionId)
          }
        />
      )}

      {/* Conflict dialog overlay */}
      {gameState?.activeConflict && (
        <ConflictDialog
          conflict={gameState.activeConflict}
          isLoading={isActionLoading}
          onChooseTactic={chooseConflictTactic}
        />
      )}
    </div>
  );
}
