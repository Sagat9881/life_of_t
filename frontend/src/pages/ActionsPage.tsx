import { useEffect } from 'react';
import { ActionList } from '../components/game/ActionList';
import { Card } from '../components/shared/Card';
import { useGameStore } from '../store/gameStore';
import '../styles/pages/ActionsPage.css';

export function ActionsPage() {
  const {
    actions,
    isLoading,
    error,
    fetchGameState,
    executeAction,
  } = useGameStore();

  useEffect(() => {
    fetchGameState();
  }, [fetchGameState]);

  return (
    <div className="actions-page">
      <Card variant="elevated" padding="large">
        <h1 className="actions-page__title">Действия</h1>
        <p className="actions-page__subtitle">
          Выберите действие, чтобы продвинуться в игре
        </p>
        
        <ActionList
          actions={actions}
          isLoading={isLoading}
          error={error}
          onExecuteAction={executeAction}
          onRetry={fetchGameState}
        />
      </Card>
    </div>
  );
}
