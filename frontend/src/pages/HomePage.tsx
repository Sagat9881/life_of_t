import { useEffect } from 'react';
import { useGameStore } from '../store/gameStore';
import { PlayerPanel } from '../components/game/PlayerPanel';
import { ActionList } from '../components/game/ActionList';
import { ConflictResolver } from '../components/game/ConflictResolver';
import { EventChoice } from '../components/game/EventChoice';
import { LoadingSpinner } from '../components/shared/LoadingSpinner';
import { ErrorMessage } from '../components/shared/ErrorMessage';
import styles from './HomePage.module.css';

/**
 * Главная страница игры
 * 
 * Отображает:
 * - Панель игрока (статы, уровень)
 * - Список доступных действий
 * - Активные конфликты (если есть)
 * - Текущие события (если есть)
 * 
 * Использует gameStore для получения актуального состояния игры
 * и выполнения действий
 */
export const HomePage = () => {
  const { 
    player, 
    actions, 
    currentConflict,
    currentEvent,
    isLoading, 
    error, 
    fetchGameState, 
    executeAction,
    selectTactic,
    selectChoice,
    cancelConflict,
    cancelEvent
  } = useGameStore();

  useEffect(() => {
    // Загрузить состояние игры при монтировании
    fetchGameState();
  }, [fetchGameState]);

  // Обработчик выполнения действия
  const handleActionExecute = async (actionCode: string) => {
    await executeAction(actionCode);
  };

  // Обработчик выбора тактики в конфликте
  const handleTacticSelect = async (tacticCode: string) => {
    await selectTactic(tacticCode);
  };

  // Обработчик выбора варианта в событии
  const handleChoiceSelect = async (choiceCode: string) => {
    await selectChoice(choiceCode);
  };

  // Loading состояние при первой загрузке
  if (isLoading && !player) {
    return (
      <div className={styles.centerContainer}>
        <LoadingSpinner size="large" />
        <p className={styles.loadingText}>Загрузка игры...</p>
      </div>
    );
  }

  // Error состояние
  if (error) {
    return (
      <div className={styles.centerContainer}>
        <ErrorMessage 
          message={error}
          onRetry={fetchGameState}
        />
      </div>
    );
  }

  // Нет данных об игроке
  if (!player) {
    return (
      <div className={styles.centerContainer}>
        <ErrorMessage 
          message="Нет данных об игроке"
          onRetry={fetchGameState}
        />
      </div>
    );
  }

  return (
    <div className={styles.homePage}>
      {/* Player Panel - всегда виден сверху */}
      <div className={styles.playerSection}>
        <PlayerPanel player={player} />
      </div>

      {/* Priority: Конфликт (если есть) */}
      {currentConflict && (
        <div className={styles.conflictSection}>
          <ConflictResolver
            conflict={currentConflict}
            isLoading={isLoading}
            onSelectTactic={handleTacticSelect}
            onCancel={cancelConflict}
          />
        </div>
      )}

      {/* Priority: Событие (если есть и нет конфликта) */}
      {!currentConflict && currentEvent && (
        <div className={styles.eventSection}>
          <EventChoice
            event={currentEvent}
            isLoading={isLoading}
            onSelectChoice={handleChoiceSelect}
            onCancel={cancelEvent}
          />
        </div>
      )}

      {/* Actions List - основной контент */}
      {!currentConflict && !currentEvent && (
        <div className={styles.actionsSection}>
          <h2 className={styles.sectionTitle}>🎯 Доступные действия</h2>
          <ActionList
            actions={actions}
            isLoading={isLoading}
            onExecuteAction={handleActionExecute}
          />
        </div>
      )}

      {/* Loading Overlay - при выполнении действий */}
      {isLoading && player && (
        <div className={styles.loadingOverlay}>
          <LoadingSpinner size="medium" />
        </div>
      )}
    </div>
  );
};
