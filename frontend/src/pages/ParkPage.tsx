import React, { useState, useEffect } from 'react';
import { useGameStore } from '../store/gameStore';
import { Character } from '../components/scene/Character';
import styles from './RoomPage.module.css';

interface ParkObject {
  id: string;
  name: string;
  actionCode: string;
  x: number;
  y: number;
  icon: string;
}

const PARK_OBJECTS: ParkObject[] = [
  { id: 'bench', name: 'Скамейка', actionCode: 'REST_ON_BENCH', x: 40, y: 70, icon: '🪑' },
  { id: 'pond', name: 'Пруд', actionCode: 'FEED_DUCKS', x: 70, y: 75, icon: '🦆' },
  { id: 'path', name: 'Дорожка', actionCode: 'JOGGING', x: 25, y: 80, icon: '🏃‍♀️' },
  { id: 'sam', name: 'Прогулка с Сэмом', actionCode: 'WALK_DOG', x: 60, y: 85, icon: '🐕' },
];

export const ParkPage: React.FC = () => {
  const { player, time, isLoading, error, fetchGameState, executeAction } = useGameStore();
  const [selectedObject, setSelectedObject] = useState<ParkObject | null>(null);

  useEffect(() => {
    fetchGameState();
  }, [fetchGameState]);

  const handleObjectClick = async (obj: ParkObject) => {
    setSelectedObject(obj);
  };

  const handleActionConfirm = async () => {
    if (!selectedObject || !player) return;
    
    try {
      await executeAction(selectedObject.actionCode);
      setSelectedObject(null);
    } catch (error: any) {
      console.error('Action failed:', error);
      alert(error.message || 'Действие не удалось');
    }
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
  const gameTime = time || { day: 1, hour: 14 };

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

      {/* Park Scene */}
      <div className={styles.room}>
        <div className={styles.roomTitle}>🌳 Парк</div>
        <div className={styles.roomScene}>
          {/* Tatyana Character */}
          <div
            style={{
              position: 'absolute',
              left: '45%',
              top: '25%',
              transform: 'translateX(-50%)',
              zIndex: 5,
            }}
          >
            <Character
              position={{ x: 0, y: 0, zIndex: 5 }}
              state="idle"
              emotion={stats.mood >= 70 ? 'happy' : stats.mood >= 40 ? 'neutral' : 'tired'}
            />
          </div>

          {/* Park Objects */}
          {PARK_OBJECTS.map((obj) => (
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
                disabled={isLoading}
              >
                {isLoading ? 'Выполняется...' : 'Да'}
              </button>
              <button
                className={styles.cancelButton}
                onClick={() => setSelectedObject(null)}
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
};
