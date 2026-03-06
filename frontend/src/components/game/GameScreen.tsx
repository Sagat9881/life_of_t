/**
 * GameScreen — main pixel-art game layout.
 *
 * Replaces the old mobile-first AppLayout+BottomNav with a desktop
 * pixel-art layout matching the original vanilla-JS demo:
 *   ┌─────────────────────────────────────────────┐
 *   │ HEADER: "LIFE OF T" logo   │  День X │ HH:00│
 *   ├───────────────────────────┬─────────────────┤
 *   │                           │  ТАНЯ — СТАТЫ   │
 *   │      PIXEL SCENE          │  Энергия ████   │
 *   │   (LocationRenderer)      │  Здоровье ███   │
 *   │                           │  ...            │
 *   │                           ├─────────────────┤
 *   │                           │ АКТИВНЫЕ КВЕСТЫ │
 *   │                           ├─────────────────┤
 *   │                           │ ОТНОШЕНИЯ       │
 *   ├───────────────────────────┴─────────────────┤
 *   │ Кликни на персонажа для взаимодействия      │
 *   └─────────────────────────────────────────────┘
 */
import { useState, useEffect } from 'react';
import { useGameStore } from '../../store/gameStore';
import { LocationRenderer } from './LocationRenderer';
import { Sidebar } from './Sidebar';
import { getLocationConfig, getLocationForTimeSlot } from '../../config/locations';
import './GameScreen.css';

const deriveTimeSlot = (hour: number): string => {
  if (hour < 7) return 'NIGHT';
  if (hour < 12) return 'MORNING';
  if (hour < 17) return 'DAY';
  if (hour < 21) return 'EVENING';
  return 'NIGHT';
};

export function GameScreen() {
  const { player, time, npcs, isLoading, error, fetchGameState, executeAction } = useGameStore();
  const [selectedObjectId, setSelectedObjectId] = useState<string | null>(null);
  const [selectedAction, setSelectedAction] = useState<{
    id: string;
    code: string;
    label: string;
  } | null>(null);

  useEffect(() => {
    fetchGameState();
  }, [fetchGameState]);

  const rawTimeSlot = time?.timeSlot ?? (time ? deriveTimeSlot(time.hour) : 'MORNING');
  const locationId = getLocationForTimeSlot(rawTimeSlot);
  const locationConfig = getLocationConfig(locationId);
  const timeOfDay = rawTimeSlot.toLowerCase();
  const gameTime = time ?? { day: 1, hour: 7, timeSlot: 'MORNING' as const };

  const handleObjectClick = (objectId: string, actionCode: string) => {
    const furniture = locationConfig.furniture.find((f) => f.id === objectId);
    setSelectedObjectId(objectId);
    setSelectedAction({
      id: objectId,
      code: actionCode,
      label: furniture?.label ?? objectId,
    });
  };

  const handleActionConfirm = async () => {
    if (!selectedAction || !player) return;
    try {
      await executeAction(selectedAction.code);
      setSelectedObjectId(null);
      setSelectedAction(null);
    } catch (err: unknown) {
      console.error('Action failed:', err instanceof Error ? err.message : err);
    }
  };

  const handleActionCancel = () => {
    setSelectedObjectId(null);
    setSelectedAction(null);
  };

  if (isLoading && !player) {
    return <div className="gs-loading">Загрузка...</div>;
  }

  if (error) {
    return <div className="gs-loading">Ошибка: {error}</div>;
  }

  if (!player) {
    return <div className="gs-loading">Нет данных об игроке</div>;
  }

  return (
    <div className="gs">
      {/* ── HEADER ── */}
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

      {/* ── MAIN AREA ── */}
      <div className="gs-main">
        {/* Scene */}
        <div className="gs-scene">
          <LocationRenderer
            config={locationConfig}
            selectedObjectId={selectedObjectId}
            onObjectClick={handleObjectClick}
            timeOfDay={timeOfDay}
          />
        </div>

        {/* Sidebar */}
        <Sidebar
          player={player}
          npcs={npcs}
          gameTime={gameTime}
        />
      </div>

      {/* ── FOOTER ── */}
      <footer className="gs-footer">
        Кликни на персонажа для взаимодействия
      </footer>

      {/* ── ACTION DIALOG ── */}
      {selectedAction ? (
        <div className="gs-dialog-overlay" onClick={handleActionCancel}>
          <div className="gs-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="gs-dialog__title">{selectedAction.label}</div>
            <div className="gs-dialog__text">Выполнить действие?</div>
            <div className="gs-dialog__buttons">
              <button
                className="gs-dialog__btn gs-dialog__btn--confirm"
                onClick={handleActionConfirm}
                disabled={isLoading}
              >
                {isLoading ? '...' : 'Да'}
              </button>
              <button
                className="gs-dialog__btn gs-dialog__btn--cancel"
                onClick={handleActionCancel}
                disabled={isLoading}
              >
                Отмена
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
