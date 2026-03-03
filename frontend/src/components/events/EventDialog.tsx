import { Sparkles } from 'lucide-react';
import { Modal } from '@/components/shared/Modal';
import { Button } from '@/components/shared/Button';
import type { GameEvent } from '@/types/game';
import styles from './EventDialog.module.css';

interface EventDialogProps {
  event: GameEvent;
  isLoading?: boolean;
  onChoose: (optionId: string) => void;
}

const OPTION_COLORS = [
  'var(--color-primary)',
  'var(--color-secondary)',
  'var(--color-accent)',
  'var(--color-success)',
];

export function EventDialog({ event, isLoading = false, onChoose }: EventDialogProps) {
  return (
    <Modal isOpen title={event.title} closeable={false}>
      <div className={styles.content}>
        <div className={styles.iconWrapper}>
          <Sparkles size={32} className={styles.icon} />
        </div>
        <p className={styles.description}>{event.description}</p>

        <div className={styles.options}>
          <h4 className={styles.optionsTitle}>Что вы сделаете?</h4>
          {event.options.map((option, i) => (
            <button
              key={option.id}
              className={styles.optionBtn}
              style={{ '--option-color': OPTION_COLORS[i % OPTION_COLORS.length] } as React.CSSProperties}
              onClick={() => onChoose(option.id)}
              disabled={isLoading}
            >
              <span className={styles.optionNumber}>{i + 1}</span>
              <div className={styles.optionContent}>
                <span className={styles.optionText}>{option.text}</span>
                {option.consequences && (
                  <span className={styles.optionConsequences}>{option.consequences}</span>
                )}
              </div>
            </button>
          ))}
        </div>
      </div>
    </Modal>
  );
}
