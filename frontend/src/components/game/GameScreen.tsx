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
import type { GameStateSnapshot } from '../../hooks/canvasTypes';

export function GameScreen() {
  const {
    player, time, availableActions, activeQuests, relationships, pets,
    npcActivities, domainEvents, isLoading, error, fetchGameState, executeAction, lastActionResult,
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

  const rawTimeSlot = time?.timeSlot ?? 'MORNING';
  const timeOfDay   = rawTimeSlot.toLowerCase();

  const locationId = useMemo(
    () => player?.location ?? getLocationForTimeSlot(rawTimeSlot),
    [player?.location, rawTimeSlot]
  );

  const locationConfig = useMemo(
    () => getLocationConfig(locationId),
    [locationId]
  );

  const gameTime       = time ?? { day: 1, hour: 7, timeSlot: 'MORNING' };
  const activeConflict = activeConflicts[0] ?? null;

  // Point override for Tanya only. NPC animations are driven by sprite-atlas conditions
  // and GameStateSnapshot.npc[...].animation — no per-slot overrides needed there.
  const characterAnimations = useMemo((): Record<string, string> => ({
    tanya: tanyaAnimOverride ?? 'idle',
  }), [tanyaAnimOverride]);

  const gameState = useMemo((): GameStateSnapshot => ({
    player: {
      energy:     player?.stats?.energy     ?? 0,
      mood:       player?.stats?.mood       ?? 0,
      health:     player?.stats?.health     ?? 0,
      money:      player?.stats?.money      ?? 0,
      stress:     player?.stats?.stress     ?? 0,
      selfEsteem: player?.stats?.selfEsteem ?? 0,
      location:   player?.location          ?? 'home',
      tags:       player?.tags              ?? {},
    },
    context: {
      time:      timeOfDay,
      day:       gameTime.day,
      hour:      gameTime.hour,
      timeSlot:  rawTimeSlot,
      dayOfWeek: ((gameTime.day - 1) % 7) + 1,
    },
    npc: Object.fromEntries(
      npcActivities.map(n => [n.npcId, { animation: n.animationKey ?? 'idle' }])
    ),
    relationships: Object.fromEntries(
      (relationships ?? []).map(r => [
        r.npcId,
        { closeness: r.closeness, trust: r.trust, stability: r.stability, romance: r.romance },
      ])
    ),
  }), [player, time, npcActivities, timeOfDay, relationships, gameTime, rawTimeSlot]);

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
            gameState={gameState}
          />
        </div>
        <div className="gs-sidebar">
          <Sidebar
            player={player}
            availableActions={availableActions}
            activeQuests={activeQuests}
            relationships={relationships}
            pets={pets}
            npcActivities={npcActivities}
            domainEvents={domainEvents}
            onActionClick={(action) => {
              setSelectedObjectId(null);
              setSelectedAction(action);
            }}
          />
        </div>
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
