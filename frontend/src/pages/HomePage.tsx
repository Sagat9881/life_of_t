import { useEffect } from 'react';
import { PlayerPanel } from '../components/game/PlayerPanel';
import { ActionList } from '../components/game/ActionList';
import { ConflictResolver } from '../components/game/ConflictResolver';
import { EventChoice } from '../components/game/EventChoice';
import { Card } from '../components/shared/Card';
import { useGameStore } from '../store/gameStore';
import { Clock } from 'lucide-react';
import { formatTime } from '../utils/formatters';
import '../styles/pages/HomePage.css';

export function HomePage() {
  const {
    player,
    time,
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
    cancelEvent,
  } = useGameStore();

  useEffect(() => {
    fetchGameState();
  }, [fetchGameState]);

  // Приоритет отображения: Событие > Конфликт > Действия
  const showEvent = currentEvent && !isLoading;
  const showConflict = currentConflict && !currentEvent && !isLoading;
  const showActions = !currentEvent && !currentConflict && !isLoading;

  return (
    <div className="home-page">
      {/* Панель игрока */}
      <div className="home-page__player-section">
        {player && <PlayerPanel player={player} />}
      </div>

      {/* Время */}
      {time && (
        <Card variant="default" padding="small" className="home-page__time-card">
          <div className="home-page__time">
            <Clock size={20} />
            <span>{formatTime(time)}</span>
          </div>
        </Card>
      )}

      {/* Контент в зависимости от состояния */}
      <div className="home-page__content">
        {showEvent && currentEvent && (
          <div className="home-page__event">
            <EventChoice
              event={currentEvent}
              isLoading={isLoading}
              error={error}
              onSelectChoice={selectChoice}
              onCancel={cancelEvent}
            />
          </div>
        )}

        {showConflict && currentConflict && (
          <div className="home-page__conflict">
            <ConflictResolver
              conflict={currentConflict}
              isLoading={isLoading}
              error={error}
              onSelectTactic={selectTactic}
              onCancel={cancelConflict}
            />
          </div>
        )}

        {showActions && (
          <div className="home-page__actions">
            <Card variant="elevated" padding="large">
              <h2 className="home-page__actions-title">Доступные действия</h2>
              <ActionList
                actions={actions}
                isLoading={isLoading}
                error={error}
                onExecuteAction={executeAction}
                onRetry={fetchGameState}
              />
            </Card>
          </div>
        )}
      </div>
    </div>
  );
}
