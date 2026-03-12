/**
 * GameScreen — main pixel-art game layout.
 * All actions come from backend (availableActions).
 */
import { useState, useEffect, useMemo } from 'react';
import { useGameStore } from '../../store/gameStore';
import { LocationRenderer } from './LocationRenderer';
import { Sidebar } from './Sidebar';
import { ConflictDialog } from './ConflictDialog';
import { EventDialog } from './EventDialog';
import { getLocationConfig, getLocationForTimeSlot } from '../../config/locations';
import type { ActionOption } from '../../types/game';
import './GameScreen.css';

export function GameScreen() {
  const {
    player, time, availableActions, activeQuests, relationships,
    npcActivities, isLoading, error, fetchGameState, executeAction, lastActionResult,
    activeConflicts, currentEvent, selectTactic, selectChoice, cancelConflict, cancelEvent,
  } = useGameStore();

  const [selectedAction, setSelectedAction] = useState<ActionOption | null>(null);
  const [selectedObjectId, setSelectedObjectId] = useState<string | null>(null);
  const [tanyaAnimOverride, setTanyaAnimOverride] = useState<string | null>(null);

  useEffect(() => { fetchGameState(); }, [fetchGameState]);

  // Авто-сброс анимации Тани через 2 секунды после действия
  useEffect(() => {
    if (!lastActionResult) return;
    const action = availableActions.find((a) => a.code === lastActionResult.actionCode);
    const anim = action?.animationKey ?? 'idle';
    setTanyaAnimOverride(anim);
    const timeout = setTimeout(() => setTanyaAnimOverride(null), 2000);
    return () => clearTimeout(timeout);
  }, [lastActionResult, availableActions]);

  const rawTimeSlot    = time?.timeSlot ?? 'MORNING';
  const locationId     = player?.location ?? getLocationForTimeSlot(rawTimeSlot);
  const locationConfig = getLocationConfig(locationId);
  const timeOfDay      = rawTimeSlot.toLowerCase();
  const gameTime       = time ?? { day: 1, hour: 7, timeSlot: 'MORNING' };

  const activeConflict = activeConflicts[0] ?? null;

  /** Record<characterSlotId, animationName> */
  const characterAnimations = useMemo((): Record<string, string> => {
    const anims: Record<string, string> = {};
    anims['tanya'] = tanyaAnimOverride ?? 'idle';
    for (const slot of locationConfig.characters) {
      if (slot.id === 'tanya') continue;
      const npcActivity = npcActivities.find((n) => n.npcId === slot.id);
      anims[slot.id] = npcActivity?.animationKey ?? 'idle';
    }
    return anims;
  }, [tanyaAnimOverride, locationConfig.characters, npcActivities]);

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

  const handleSelectTactic = async (tacticCode: string): Promise<void> => {
    if (!activeConflict) return;
    await selectTactic(activeConflict.id, tacticCode);
  };

  const handleSelectChoice = async (optionCode: string): Promise<void> => {
    if (!currentEvent) return;
    await selectChoice(currentEvent.id, optionCode);
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

      {activeConflict && (
        <ConflictDialog
          conflict={activeConflict}
          isLoading={isLoading}
          onSelectTactic={handleSelectTactic}
          onRetreat={cancelConflict}
        />
      )}

      {!activeConflict && currentEvent && (
        <EventDialog
          event={currentEvent}
          isLoading={isLoading}
          onSelectOption={handleSelectChoice}
          onClose={cancelEvent}
        />
      )}

      {!activeConflict && !currentEvent && selectedAction && (
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
