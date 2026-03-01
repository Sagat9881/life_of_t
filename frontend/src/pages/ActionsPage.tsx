import { useGameStore } from '../store/gameStore';
import { ActionList } from '../components/game/ActionList';
import { BottomNav } from '../components/layout/BottomNav/BottomNav';
import './ActionsPage.css';

export function ActionsPage() {
  const { actions, executeAction, isLoading } = useGameStore();

  const handleExecuteAction = async (actionCode: string) => {
    try {
      await executeAction(actionCode);
    } catch (error) {
      console.error('Failed to execute action:', error);
    }
  };

  return (
    <div className="actions-page">
      <div className="actions-page__content">
        <header className="actions-page__header">
          <h1 className="actions-page__title">⚡ Действия</h1>
          <p className="actions-page__subtitle">
            Выберите действие для выполнения
          </p>
        </header>

        <div className="actions-page__list">
          {isLoading ? (
            <div className="actions-page__loading">Загрузка...</div>
          ) : (
            <ActionList actions={actions} onExecuteAction={handleExecuteAction} />
          )}
        </div>
      </div>

      <BottomNav current="actions" />
    </div>
  );
}
