import { Clock, X } from 'lucide-react';
import type { ActionOption } from '@/types/game';
import styles from './ActionMenu.module.css';

const ACTION_ICONS: Record<string, string> = {
  go_to_work: '💼',
  jogging: '🏃',
  make_coffee: '☕',
  call_husband: '📞',
  date_with_husband: '❤️',
  feed_ducks: '🦆',
  walk_dog: '🐕',
  play_with_cat: '🐱',
  rest_at_home: '🛋️',
  rest_on_bench: '🪑',
  household: '🧹',
  self_care: '💅',
  talk_to_colleague: '💬',
  visit_father: '👨',
};

interface ActionMenuProps {
  actions: ActionOption[];
  onSelect: (id: string) => void;
  onClose: () => void;
  pendingAction: string | null;
}

export function ActionMenu({ actions, onSelect, onClose, pendingAction }: ActionMenuProps) {
  return (
    <div className={styles.menu}>
      <div className={styles.arrow} />
      <div className={styles.header}>
        <span className={styles.title}>Действия</span>
        <button className={styles.closeBtn} onClick={onClose}>
          <X size={14} />
        </button>
      </div>
      <div className={styles.actions}>
        {actions.map(action => (
          <button
            key={action.id}
            className={styles.actionBtn}
            data-disabled={!action.available || !!pendingAction}
            data-pending={pendingAction === action.id}
            onClick={() => action.available && !pendingAction && onSelect(action.id)}
          >
            <span className={styles.actionIcon}>{ACTION_ICONS[action.id] ?? '✨'}</span>
            <div className={styles.actionInfo}>
              <div className={styles.actionName}>{action.displayName}</div>
            </div>
            <div className={styles.actionCost}>
              <Clock size={10} />
              <span>{action.energyCost}</span>
            </div>
          </button>
        ))}
      </div>
    </div>
  );
}
