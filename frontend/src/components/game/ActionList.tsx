import { useState, useMemo } from 'react';
import { Search } from 'lucide-react';
import { ActionCard } from './ActionCard';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import type { ActionOption } from '../../types/game';
import '../../styles/components/ActionList.css';

interface ActionListProps {
  actions: ActionOption[];
  isLoading?: boolean;
  error?: string | null;
  onExecuteAction?: (actionCode: string) => void;
  onRetry?: () => void;
}

export function ActionList({
  actions = [],
  isLoading = false,
  error = null,
  onExecuteAction,
  onRetry,
}: ActionListProps) {
  const [searchQuery, setSearchQuery] = useState('');

  const filteredActions = useMemo(() => {
    return (actions || []).filter(action => {
      if (!action) return false;
      const label = action.label || '';
      const desc = action.description || '';
      return label.toLowerCase().includes(searchQuery.toLowerCase()) ||
             desc.toLowerCase().includes(searchQuery.toLowerCase());
    });
  }, [actions, searchQuery]);

  if (isLoading) {
    return (
      <div className="action-list__loading">
        <LoadingSpinner size="large" text="Загрузка действий..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className="action-list__error">
        <ErrorMessage message={error} {...(onRetry && { onRetry })} />
      </div>
    );
  }

  return (
    <div className="action-list">
      <div className="action-list__filters">
        <div className="action-list__search">
          <Search size={20} />
          <input
            type="text"
            placeholder="Поиск действий..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="action-list__search-input"
          />
        </div>
      </div>

      <div className="action-list__grid">
        {filteredActions.length > 0 ? (
          filteredActions.map(action => (
            <ActionCard
              key={action.code}
              action={action}
              {...(onExecuteAction && { onExecute: onExecuteAction })}
            />
          ))
        ) : (
          <div className="action-list__empty">
            <p>Действия не найдены</p>
            {searchQuery && (
              <button
                onClick={() => setSearchQuery('')}
                className="action-list__clear-search"
              >
                Очистить поиск
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
