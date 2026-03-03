import { useState, useMemo } from 'react';
import { Search } from 'lucide-react';
import { ActionCard } from './ActionCard';
import { LoadingSpinner } from '@/components/shared/LoadingSpinner';
import type { ActionOption } from '@/types/game';
import styles from './ActionList.module.css';

interface ActionListProps {
  actions: ActionOption[];
  isLoading?: boolean;
  onExecute?: (actionType: string) => void;
  loadingActionType?: string;
}

export function ActionList({ actions, isLoading = false, onExecute, loadingActionType }: ActionListProps) {
  const [search, setSearch] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('all');

  const categories = useMemo(() => {
    const cats = new Set<string>();
    actions.forEach(a => { if (a.category) cats.add(a.category); });
    return ['all', ...Array.from(cats)];
  }, [actions]);

  const filtered = useMemo(() => {
    return actions.filter(action => {
      const matchSearch = search === '' ||
        action.displayName.toLowerCase().includes(search.toLowerCase()) ||
        action.description.toLowerCase().includes(search.toLowerCase());
      const matchCat = categoryFilter === 'all' || action.category === categoryFilter;
      return matchSearch && matchCat;
    });
  }, [actions, search, categoryFilter]);

  if (isLoading) {
    return (
      <div className={styles.loadingWrapper}>
        <LoadingSpinner size="lg" text="Загрузка действий..." />
      </div>
    );
  }

  return (
    <div className={styles.list}>
      {/* Search */}
      <div className={styles.searchWrapper}>
        <Search size={16} className={styles.searchIcon} />
        <input
          type="text"
          placeholder="Поиск действий..."
          value={search}
          onChange={e => setSearch(e.target.value)}
          className={styles.searchInput}
        />
      </div>

      {/* Category filters */}
      {categories.length > 2 && (
        <div className={styles.filters}>
          {categories.map(cat => (
            <button
              key={cat}
              className={`${styles.filterBtn} ${categoryFilter === cat ? styles.filterActive : ''}`}
              onClick={() => setCategoryFilter(cat)}
            >
              {cat === 'all' ? 'Все' : cat}
            </button>
          ))}
        </div>
      )}

      {/* Actions */}
      {filtered.length === 0 ? (
        <div className={styles.empty}>
          <p>Нет доступных действий</p>
          {search && (
            <button className={styles.clearSearch} onClick={() => setSearch('')}>
              Сбросить поиск
            </button>
          )}
        </div>
      ) : (
        <div className={styles.grid}>
          {filtered.map(action => (
            <ActionCard
              key={action.type}
              action={action}
              onExecute={onExecute}
              isLoading={loadingActionType === action.type}
            />
          ))}
        </div>
      )}
    </div>
  );
}
