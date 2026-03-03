import { useGameStore } from '@/store/gameStore';
import { ActionList } from '@/components/actions/ActionList';
import styles from './ActionsPage.module.css';

export function ActionsPage() {
  const { gameState, isLoading, isActionLoading, executeAction } = useGameStore();

  const actions = gameState?.availableActions ?? [];

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1 className={styles.title}>⚡ Действия</h1>
        {gameState && (
          <span className={styles.timeLeft}>
            День {gameState.time.day} • {String(gameState.time.hour).padStart(2, '0')}:00
          </span>
        )}
      </div>

      <ActionList
        actions={actions}
        isLoading={isLoading && !gameState}
        onExecute={executeAction}
        loadingActionType={isActionLoading ? '__loading__' : undefined}
      />
    </div>
  );
}
