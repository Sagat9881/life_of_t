import type { EventView } from '../../types/game';

interface EventDialogProps {
  readonly event: EventView;
  readonly isLoading: boolean;
  readonly onSelectOption: (optionCode: string) => void;
  readonly onClose: () => void;
}

const SPEAKER_COLORS: Record<string, string> = {
  tanya: '#f093fb',
  sam: '#4fc3f7',
  narrator: '#c0b8d0',
};

function getSpeakerColor(speaker: string): string {
  return SPEAKER_COLORS[speaker.toLowerCase()] ?? '#e0d8f0';
}

export function EventDialog({ event, isLoading, onSelectOption, onClose }: EventDialogProps) {
  return (
    <div className="gs-dialog-overlay">
      <div className="gs-dialog gs-event" onClick={(e) => e.stopPropagation()}>
        <div className="gs-dialog__title">{event.titleRu}</div>

        {event.descriptionRu && (
          <div className="gs-event__description">{event.descriptionRu}</div>
        )}

        {event.dialogue.length > 0 && (
          <div className="gs-event__dialogue">
            {event.dialogue.map((line, idx) => (
              <div key={idx} className="gs-event__line">
                <span
                  className="gs-event__speaker"
                  style={{ color: getSpeakerColor(line.speaker) }}
                >
                  {line.speaker}:
                </span>
                <span className="gs-event__text">{line.textRu}</span>
              </div>
            ))}
          </div>
        )}

        {event.options.length > 0 && (
          <>
            <div className="gs-event__choices-label">Выбор</div>
            <div className="gs-event__choices">
              {event.options.map((option) => (
                <button
                  key={option.code}
                  className="gs-event__choice"
                  disabled={isLoading}
                  onClick={() => onSelectOption(option.code)}
                >
                  {option.labelRu}
                </button>
              ))}
            </div>
          </>
        )}

        <div className="gs-dialog__buttons">
          <button
            className="gs-dialog__btn gs-dialog__btn--cancel"
            disabled={isLoading}
            onClick={onClose}
          >
            Закрыть
          </button>
        </div>
      </div>
    </div>
  );
}
