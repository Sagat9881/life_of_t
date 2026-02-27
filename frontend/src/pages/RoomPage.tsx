import React, { useState } from 'react';
import { useGameState } from '../hooks/useGameState';
import { executeAction } from '../api/game';
import styles from './RoomPage.module.css';

interface RoomObject {
  id: string;
  name: string;
  actionCode: string;
  x: number;
  y: number;
  icon: string;
}

const ROOM_OBJECTS: RoomObject[] = [
  { id: 'bed', name: 'Кровать', actionCode: 'REST_AT_HOME', x: 20, y: 30, icon: '🛏️' },
  { id: 'computer', name: 'Компьютер', actionCode: 'WORK_ON_PROJECT', x: 70, y: 35, icon: '💻' },
  { id: 'phone', name: 'Телефон', actionCode: 'CALL_HUSBAND', x: 50, y: 60, icon: '📱' },
  { id: 'mirror', name: 'Зеркало', actionCode: 'BEAUTY_ROUTINE', x: 80, y: 70, icon: '🪞' },
  { id: 'dogs', name: 'Собаки', actionCode: 'WALK_DOG', x: 30, y: 70, icon: '🐕' },
];

export const RoomPage: React.FC = () => {
  const { gameState, refreshGameState } = useGameState();
  const [selectedObject, setSelectedObject] = useState<RoomObject | null>(null);
  const [isExecuting, setIsExecuting] = useState(false);

  const handleObjectClick = async (obj: RoomObject) => {
    setSelectedObject(obj);
  };

  const handleActionConfirm = async () => {
    if (!selectedObject || !gameState) return;
    
    setIsExecuting(true);
    try {
      await executeAction(gameState.telegramUserId, selectedObject.actionCode);
      await refreshGameState();
      setSelectedObject(null);
    } catch (error: any) {
      console.error('Action failed:', error);
      alert(error.response?.data?.message || 'Действие не удалось');
    } finally {
      setIsExecuting(false);
    }
  };

  if (!gameState) {
    return <div className={styles.loading}>Загрузка...</div>;
  }

  return (
    <div className={styles.roomContainer}>
      {/* HUD - Stats Bar */}
      <div className={styles.hud}>
        <div className={styles.hudLeft}>
          <div className={styles.stat}>
            <span className={styles.statIcon}>⚡</span>
            <span className={styles.statValue}>{gameState.player.energy}/100</span>
          </div>
          <div className={styles.stat}>
            <span className={styles.statIcon}>❤️</span>
            <span className={styles.statValue}>{gameState.player.health}/100</span>
          </div>
          <div className={styles.stat}>
            <span className={styles.statIcon}>😊</span>
            <span className={styles.statValue}>{gameState.player.mood}/100</span>
          </div>
        </div>
        <div className={styles.hudCenter}>
          <div className={styles.timeDisplay}>
            <span className={styles.day}>День {gameState.time.day}</span>
            <span className={styles.hour}>{gameState.time.hour}:00</span>
          </div>
        </div>
        <div className={styles.hudRight}>
          <div className={styles.money}>
            <span className={styles.moneyIcon}>💰</span>
            <span className={styles.moneyValue}>{gameState.player.money} ₽</span>
          </div>
        </div>
      </div>

      {/* Room with Objects */}
      <div className={styles.room}>
        <div className={styles.roomTitle}>Комната Татьяны</div>
        <div className={styles.roomScene}>
          {ROOM_OBJECTS.map((obj) => (
            <button
              key={obj.id}
              className={`${styles.roomObject} ${
                selectedObject?.id === obj.id ? styles.selected : ''
              }`}
              style={{ left: `${obj.x}%`, top: `${obj.y}%` }}
              onClick={() => handleObjectClick(obj)}
            >
              <div className={styles.objectIcon}>{obj.icon}</div>
              <div className={styles.objectLabel}>{obj.name}</div>
            </button>
          ))}
        </div>
      </div>

      {/* Action Dialog */}
      {selectedObject && (
        <div className={styles.actionDialog}>
          <div className={styles.dialogContent}>
            <h3>{selectedObject.name}</h3>
            <p>Выполнить действие?</p>
            <div className={styles.dialogButtons}>
              <button
                className={styles.confirmButton}
                onClick={handleActionConfirm}
                disabled={isExecuting}
              >
                {isExecuting ? 'Выполняется...' : 'Да'}
              </button>
              <button
                className={styles.cancelButton}
                onClick={() => setSelectedObject(null)}
                disabled={isExecuting}
              >
                Отмена
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
