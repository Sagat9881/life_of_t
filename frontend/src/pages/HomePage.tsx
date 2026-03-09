import { useEffect } from 'react';
import { useGameStore } from '../store/gameStore';
import { PlayerPanel } from '../components/game/PlayerPanel';
import { ActionList } from '../components/game/ActionList';
import { ConflictResolver } from '../components/game/ConflictResolver';
import { EventChoice } from '../components/game/EventChoice';
import { LoadingSpinner } from '../components/shared/LoadingSpinner';
import { ErrorMessage } from '../components/shared/ErrorMessage';
import styles from './HomePage.module.css';

export const HomePage = () => {
  const {
    player, actions, activeConflicts, currentEvent,
    isLoading, error, fetchGameState, executeAction,
    selectTactic, selectChoice, cancelConflict, cancelEvent
  } = useGameStore();

  const currentConflict = activeConflicts.length > 0 ? activeConflicts[0] : null;

  useEffect(() => { fetchGameState(); }, [fetchGameState]);

  const handleActionExecute = async (actionCode: string) => { await executeAction(actionCode); };
  const handleTacticSelect = async (conflictId: string, tacticCode: string) => { await selectTactic(conflictId, tacticCode); };
  const handleChoiceSelect = async (eventId: string, choiceCode: string) => { await selectChoice(eventId, choiceCode); };

  if (isLoading && !player) return <div className={styles.centerContainer}><LoadingSpinner size="large" /><p className={styles.loadingText}>Загрузка игры...</p></div>;
  if (error) return <div className={styles.centerContainer}><ErrorMessage message={error} onRetry={fetchGameState} /></div>;
  if (!player) return <div className={styles.centerContainer}><ErrorMessage message="Нет данных об игроке" onRetry={fetchGameState} /></div>;

  return (
    <div className={styles.homePage}>
      <div className={styles.playerSection}><PlayerPanel player={player} /></div>
      {currentConflict && (
        <div className={styles.conflictSection}>
          <ConflictResolver conflict={currentConflict} isLoading={isLoading} onSelectTactic={handleTacticSelect} onCancel={cancelConflict} />
        </div>
      )}
      {!currentConflict && currentEvent && (
        <div className={styles.eventSection}>
          <EventChoice event={currentEvent} isLoading={isLoading} onSelectChoice={handleChoiceSelect} onCancel={cancelEvent} />
        </div>
      )}
      {!currentConflict && !currentEvent && (
        <div className={styles.actionsSection}>
          <h2 className={styles.sectionTitle}>🎯 Доступные действия</h2>
          <ActionList actions={actions} isLoading={isLoading} onExecuteAction={handleActionExecute} />
        </div>
      )}
      {isLoading && player && <div className={styles.loadingOverlay}><LoadingSpinner size="medium" /></div>}
    </div>
  );
};
