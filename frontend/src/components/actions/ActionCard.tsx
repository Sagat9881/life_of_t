import { Clock, AlertCircle } from 'lucide-react';
import type { ActionOption } from '../../types/game';
import styles from './ActionCard.module.css';

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

interface ActionCardProps {
  action: ActionOption;
  onSelect: (id: string) => void;
  isPending: boolean;
}

export function ActionCard({ action, onSelect, isPending }: ActionCardProps) {
  const icon = ACTION_ICONS[action.id] ?? '✨';
  const isDisabled = !action.available || isPending;

  return (
    <button
      className={styles.card}
      data-disabled={isDisabled}
      data-pending={isPending}
      onClick={() => !isDisabled && onSelect(action.id)}
      aria-label={`${action.displayName} — ${action.energyCost} энергии`}
    >
      <span className={styles.icon}>{icon}</span>
      <div className={styles.info}>
        <div className={styles.name}>{action.displayName}</div>
        {action.available ? (
          <div className={styles.description}>{action.description}</div>
        ) : (
          <div className={styles.unavailableReason}>
            <AlertCircle size={10} style={{ display: 'inline', marginRight: 3 }} />
            {action.unavailableReason ?? 'Недоступно'}
          </div>
        )}
      </div>
      <div className={styles.cost}>
        <Clock size={12} />
        <span>{action.energyCost}</span>
      </div>
    </button>
  );
}
