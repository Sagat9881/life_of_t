import React, { useState, useEffect } from 'react';
import { useGameStore } from '../store/gameStore';
import { LocationRenderer } from '../components/game/LocationRenderer';
import { getLocationConfig, getLocationForTimeSlot } from '../config/locations';
import styles from './RoomPage.module.css';

export const RoomPage: React.FC = () => {
  const { player, time, isLoading, error, fetchGameState, executeAction } = useGameStore();
  const [selectedObjectId, setSelectedObjectId] = useState<string | null>(null);
  const [selectedAction, setSelectedAction] = useState<{ id: string; code: string; label: string } | null>(null);

  useEffect(() => {
    fetchGameState();
  }, [fetchGameState]);

  // Determine current location based on time slot
  const locationId = time ? getLocationForTimeSlot(time.timeSlot) : 'home_room';
  const locationConfig = getLocationConfig(locationId);

  // Derive time-of-day condition from game time
  const timeOfDay = time ? time.timeSlot.toLowerCase() : 'morning';

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
      const message = err instanceof Error ? err.message : 'Действие не удалось';
      console.error('Action failed:', message);
    }
  };

  const handleActionCancel = () => {
    setSelectedObjectId(null);
    setSelectedAction(null);
  };

  if (isLoading && !player) {
    return <div className={styles.loading}>Загрузка...</div>;
  }

  if (error) {
    return <div className={styles.loading}>Ошибка: {error}</div>;
  }

  if (!player) {
    return <div className={styles.loading}>Нет данных об игроке</div>;
  }

  const stats = player.stats;
  const gameTime = time ?? { day: 1, hour: 7, timeSlot: 'MORNING' as const };

  return (
    <div className={styles.roomContainer}>
      {/* HUD - Stats Bar */}
      <div className={styles.hud}>
        <div className={styles.hudLeft}>
          <div className={styles.stat}>
            <span className={styles.statIcon}>⚡</span>
            <span className={styles.statValue}>{stats.energy}/100</span>
          </div>
          <div className={styles.stat}>
            <span className={styles.statIcon}>❤️</span>
            <span className={styles.statValue}>{stats.health}/100</span>
          </div>
          <div className={styles.stat}>
            <span className={styles.statIcon}>😊</span>
            <span className={styles.statValue}>{stats.mood}/100</span>
          </div>
        </div>
        <div className={styles.hudCenter}>
          <div className={styles.timeDisplay}>
            <span className={styles.day}>День {gameTime.day}</span>
            <span className={styles.hour}>{gameTime.hour}:00</span>
          </div>
        </div>
        <div className={styles.hudRight}>
          <div className={styles.money}>
            <span className={styles.moneyIcon}>💰</span>
            <span className={styles.moneyValue}>{stats.money} ₽</span>
          </div>
        </div>
      </div>

      {/* Pixel-art Scene */}
      <div className={styles.room}>
        <div className={styles.roomTitle}>✨ {locationConfig.name} ✨</div>
        <div className={styles.sceneWrapper}>
          <LocationRenderer
            config={locationConfig}
            selectedObjectId={selectedObjectId}
            onObjectClick={handleObjectClick}
            timeOfDay={timeOfDay}
          />
        </div>
      </div>

      {/* Action Dialog */}
      {selectedAction ? (
        <div className={styles.actionDialog}>
          <div className={styles.dialogContent}>
            <h3>{selectedAction.label}</h3>
            <p>Выполнить действие?</p>
            <div className={styles.dialogButtons}>
              <button
                className={styles.confirmButton}
                onClick={handleActionConfirm}
                disabled={isLoading}
              >
                {isLoading ? 'Выполняется...' : 'Да'}
              </button>
              <button
                className={styles.cancelButton}
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
};
