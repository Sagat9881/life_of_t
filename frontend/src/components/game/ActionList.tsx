import { useState, useMemo } from 'react';
import { Search } from 'lucide-react';
import { ActionCard } from './ActionCard';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import type { GameAction } from '../../types/game';
import '../../styles/components/ActionList.css';

interface ActionListProps {
  actions: GameAction[];
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
  const [categoryFilter, setCategoryFilter] = useState<string>('all');

  const categories = useMemo(() => {
    const cats = new Set(['all']);
    (actions || []).forEach(action => {
      if (action?.category) cats.add(action.category);
    });
    return Array.from(cats);
  }, [actions]);

  const filteredActions = useMemo(() => {
    return (actions || []).filter(action => {
      if (!action) return false;
      
      const actionName = action.name || '';
      const actionDesc = action.description || '';
      const actionCategory = action.category || '';
      
      const matchesSearch = actionName.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          actionDesc.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesCategory = categoryFilter === 'all' || actionCategory === categoryFilter;
      return matchesSearch && matchesCategory;
    });
  }, [actions, searchQuery, categoryFilter]);

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

        {categories.length > 1 && (
          <div className="action-list__categories">
            {categories.map(category => (
              <button
                key={category}
                onClick={() => setCategoryFilter(category)}
                className={`action-list__category-button ${
                  categoryFilter === category ? 'action-list__category-button--active' : ''
                }`}
              >
                {category === 'all' ? 'Все' : category}
              </button>
            ))}
          </div>
        )}
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
