/**
 * GameScreen — main pixel-art game layout.
 * All actions come from backend (availableActions).
 */
import { useState, useEffect, useMemo } from 'react';
import { useGameStore } from '../../store/gameStore';
import { LocationRenderer } from './LocationRenderer';
import { Sidebar } from './Sidebar';
import { getLocationConfig, getLocationForTimeSlot } from '../../config/locations';
import type { ActionOption } from '../../types/game';
import './GameScreen.css';

const deriveTimeSlot = (hour: number): string => {
  if (hour < 7)  return 'NIGHT';
  if (hour < 12) return 'MORNING';
  if (hour < 17) return 'DAY';
  if (hour < 21) return 'EVENING';
  return 'NIGHT';
};

/** Maps backend action codes to character animation names. */
const ACTION_CODE_TO_ANIMATION: Record<string, string> = {
  REST_AT_HOME:    'sleep',
  SLEEP:           'sleep',
  WORK_ON_PROJECT: 'work',
  STUDY:           'work',
  COOK_FOOD:       'work',
  EXERCISE:        'exercise',
  WALK_DOG:        'walk',
  GO_FOR_WALK:     'walk',
};

function resolveAnimation(action?: ActionOption | null): string {
  if (!action) return 'idle';
  if (action.animationKey) return action.animationKey;
  return ACTION_CODE_TO_ANIMATION[action.code] ?? 'idle';
}

export function GameScreen() {
  const {
    player, time, availableActions, activeQuests, relationships,
    isLoading, error, fetchGameState, executeAction, lastActionResult,
  } = useGameStore();

  const [selectedAction, setSelectedAction] = useState<ActionOption | null>(null);
  const [selectedObjectId, setSelectedObjectId] = useState<string | null>(null);

  useEffect(() => { fetchGameState(); }, [fetchGameState]);

  const rawTimeSlot    = time?.timeSlot ?? (time ? deriveTimeSlot(time.hour) : 'MORNING');
  const locationId     = getLocationForTimeSlot(rawTimeSlot);
  const locationConfig = getLocationConfig(locationId);
  const timeOfDay      = rawTimeSlot.toLowerCase();
  const gameTime       = time ?? { day: 1, hour: 7, timeSlot: 'MORNING' as const };

  /** Record<characterSlotId, animationName> */
  const characterAnimations = useMemo((): Record<string, string> => {
    const anims: Record<string, string> = {};
    anims['tanya'] = resolveAnimation(
      lastActionResult
        ? availableActions.find((a) => a.code === lastActionResult.actionCode)
        : null
    );
    for (const slot of locationConfig.characters) {
      if (slot.id !== 'tanya' && !anims[slot.id]) {
        anims[slot.id] = 'idle';
      }
    }
    return anims;
  }, [lastActionResult, availableActions, locationConfig.characters]);

  const handleObjectClick = (objectId: string, actionCode: string): void => {
    setSelectedObjectId(objectId);
    const backendAction = availableActions.find((a) => a.code === actionCode);
    if (backendAction) setSelectedAction(backendAction);
  };

  const handleDialogClose = (): void => {
    setSelectedAction(null);
    setSelectedObjectId(null);
  };

  const handleActionConfirm = async (): Promise<void> => {
    if (!selectedAction || !player) return;
    try {
      await executeAction(selectedAction.code);
      handleDialogClose();
    } catch (err: unknown) {
      console.error('Action failed:', err instanceof Error ? err.message : err);
    }
  };

  if (isLoading && !player) return <div className="gs-loading">Загрузка...</div>;
  if (error)               return <div className="gs-loading">Ошибка: {error}</div>;
  if (!player)             return <div className="gs-loading">Нет данных об игроке</div>;

  return (
    <div className="gs">
      <header className="gs-header">
        <div className="gs-header__left">
          <span className="gs-header__logo">LIFE OF T</span>
          <span className="gs-header__version">Demo v0.9</span>
        </div>
        <div className="gs-header__right">
          <span className="gs-header__time">
            День {gameTime.day} | {String(gameTime.hour).padStart(2, '0')}:00
          </span>
        </div>
      </header>

      <div className="gs-main">
        <div className="gs-scene">
          <LocationRenderer
            config={locationConfig}
            selectedObjectId={selectedObjectId}
            onObjectClick={handleObjectClick}
            characterAnimations={characterAnimations}
            timeOfDay={timeOfDay}
          />
        </div>
        <Sidebar
          player={player}
          availableActions={availableActions}
          activeQuests={activeQuests}
          relationships={relationships}
          onActionClick={(action) => {
            setSelectedObjectId(null);
            setSelectedAction(action);
          }}
        />
      </div>

      <footer className="gs-footer">Кликни на предмет или выбери действие</footer>

      {selectedAction && (
        <div className="gs-dialog-overlay" onClick={handleDialogClose}>
          <div className="gs-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="gs-dialog__title">{selectedAction.label}</div>
            <div className="gs-dialog__text">
              {selectedAction.description}<br />
              <span className="gs-dialog__cost">⏱ {selectedAction.estimatedTimeCost}ч</span>
            </div>
            <div className="gs-dialog__buttons">
              <button
                className="gs-dialog__btn gs-dialog__btn--confirm"
                onClick={handleActionConfirm}
                disabled={isLoading || !selectedAction.isAvailable}
              >
                {isLoading
                  ? '...'
                  : selectedAction.isAvailable
                    ? 'Да'
                    : selectedAction.unavailableReason ?? 'Недоступно'}
              </button>
              <button
                className="gs-dialog__btn gs-dialog__btn--cancel"
                onClick={handleDialogClose}
                disabled={isLoading}
              >
                Отмена
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
