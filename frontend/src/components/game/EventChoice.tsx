import { Sparkles, X } from 'lucide-react';
import { ChoiceButton } from './ChoiceButton';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import type { GameEvent } from '../../types/game';
import '../../styles/components/EventChoice.css';

interface EventChoiceProps {
  event: GameEvent;
  isLoading?: boolean;
  error?: string | null;
  onSelectChoice?: (choiceCode: string) => void;
  onCancel?: () => void;
  onRetry?: () => void;
}

export function EventChoice({
  event,
  isLoading = false,
  error = null,
  onSelectChoice,
  onCancel,
  onRetry,
}: EventChoiceProps) {
  const { title, description, choices } = event;

  if (isLoading) {
    return (
      <div className="event-choice event-choice--loading">
        <LoadingSpinner size="large" text="Обработка события..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className="event-choice event-choice--error">
        <ErrorMessage message={error} {...(onRetry && { onRetry })} />
      </div>
    );
  }

  return (
    <div className="event-choice">
      {onCancel && (
        <button
          onClick={onCancel}
          className="event-choice__close"
          aria-label="Закрыть"
        >
          <X size={24} />
        </button>
      )}

      <div className="event-choice__header">
        <div className="event-choice__icon">
          <Sparkles size={32} />
        </div>
        <h2 className="event-choice__title">{title}</h2>
      </div>

      <div className="event-choice__description">
        <p>{description}</p>
      </div>

      <div className="event-choice__choices">
        <h3 className="event-choice__choices-title">Что вы сделаете?</h3>
        <div className="event-choice__choices-list">
          {choices.map((choice, index) => (
            <ChoiceButton
              key={choice.code}
              choice={choice}
              index={index}
              {...(onSelectChoice && { onSelect: onSelectChoice })}
            />
          ))}
        </div>
      </div>

      {choices.length === 0 && (
        <div className="event-choice__empty">
          <p>Нет доступных вариантов</p>
        </div>
      )}
    </div>
  );
}
