import { useState, useRef, useEffect } from 'react';
import { useGameStore } from '@/store/gameStore';
import { InteractiveObject } from './InteractiveObject';
import { ActionMenu } from './ActionMenu';
import type { ActionOption } from '@/types/game';
import styles from './RoomScene.module.css';

type TimeOfDay = 'morning' | 'day' | 'evening' | 'night';

function getTimeOfDay(hour: number): TimeOfDay {
  if (hour >= 6 && hour < 12) return 'morning';
  if (hour >= 12 && hour < 18) return 'day';
  if (hour >= 18 && hour < 22) return 'evening';
  return 'night';
}

interface RoomObject {
  id: string;
  emoji: string;
  label: string;
  x: number;
  y: number;
  width: number;
  height: number;
  actions: string[];  // action ids that this object triggers
}

const ROOM_OBJECTS: RoomObject[] = [
  { id: 'sofa',      emoji: '🛋️', label: 'Диван',         x: 15, y: 55, width: 70, height: 55, actions: ['rest_at_home'] },
  { id: 'kitchen',   emoji: '🍳', label: 'Кухня',         x: 75, y: 45, width: 55, height: 55, actions: ['make_coffee'] },
  { id: 'computer',  emoji: '💻', label: 'Компьютер',     x: 55, y: 38, width: 45, height: 45, actions: ['go_to_work'] },
  { id: 'phone',     emoji: '📱', label: 'Телефон',       x: 40, y: 60, width: 30, height: 30, actions: ['call_husband'] },
  { id: 'bed',       emoji: '🛏️', label: 'Кровать',         x: 82, y: 60, width: 55, height: 45, actions: ['rest_at_home'] },
  { id: 'wardrobe',  emoji: '👗', label: 'Шкаф',           x: 5,  y: 40, width: 40, height: 55, actions: ['self_care'] },
  { id: 'door',      emoji: '🚪', label: 'Выход',           x: 88, y: 28, width: 35, height: 50, actions: ['go_to_work', 'jogging', 'visit_father'] },
];

export function RoomScene() {
  const { gameState, availableActions, pendingAction, executeAction } = useGameStore();
  const [selectedObject, setSelectedObject] = useState<string | null>(null);
  const [menuActions, setMenuActions] = useState<ActionOption[]>([]);
  const [feedback, setFeedback] = useState<string | null>(null);
  const sceneRef = useRef<HTMLDivElement>(null);

  // Close menu on outside click
  useEffect(() => {
    const handleOutsideClick = (e: MouseEvent) => {
      if (sceneRef.current && !sceneRef.current.contains(e.target as Node)) {
        setSelectedObject(null);
        setMenuActions([]);
      }
    };
    document.addEventListener('mousedown', handleOutsideClick);
    return () => document.removeEventListener('mousedown', handleOutsideClick);
  }, []);

  const handleObjectClick = (objectId: string) => {
    const obj = ROOM_OBJECTS.find(o => o.id === objectId);
    if (!obj || !gameState) return;

    // Find actions available for this object
    const objActions = availableActions.filter(
      a => obj.actions.includes(a.id)
    );

    if (objActions.length === 0) return;

    if (selectedObject === objectId) {
      // Deselect
      setSelectedObject(null);
      setMenuActions([]);
    } else {
      setSelectedObject(objectId);
      setMenuActions(objActions);
    }
  };

  const handleActionSelect = async (actionId: string) => {
    setSelectedObject(null);
    setMenuActions([]);

    try {
      await executeAction(actionId);
      // Show feedback
      const action = availableActions.find(a => a.id === actionId);
      if (action) {
        setFeedback(`✅ ${action.displayName}`);
        setTimeout(() => setFeedback(null), 2000);
      }
    } catch {
      setFeedback('❌ Ошибка');
      setTimeout(() => setFeedback(null), 2000);
    }
  };

  const tod = gameState ? getTimeOfDay(gameState.time.hour) : 'day';
  const location = gameState?.player.location ?? 'home';
  const hour = gameState?.time.hour ?? 9;

  const LOCATION_LABELS: Record<string, string> = {
    home: '🏠 Дом',
    work: '🏗️ Работа',
    park: '🌳 Парк',
    street: '🚭 Улица',
    cafe: '☕ Кафе',
  };

  return (
    <div
      ref={sceneRef}
      className={`${styles.scene} ${styles[tod]}`}
      data-testid="room-scene"
    >
      {/* Background layers */}
      <div className={styles.walls}>
        <div className={styles.wallPattern} />
      </div>
      <div className={styles.floor}>
        <div className={styles.floorPattern} />
      </div>
      <div className={styles.shadowLine} />

      {/* Window */}
      <div className={styles.window}>
        <div className={styles.windowFrame}>
          <div className={styles.windowPane} />
          <div className={styles.windowPane} />
          <div className={styles.windowPane} />
          <div className={styles.windowPane} />
        </div>
        <div className={styles.windowSill} />
      </div>

      {/* Night overlay */}
      {tod === 'night' && <div className={styles.nightOverlay} />}

      {/* Interactive objects */}
      <div className={styles.objectsLayer}>
        {ROOM_OBJECTS.map(obj => {
          const hasAvailableActions = availableActions.some(
            a => obj.actions.includes(a.id)
          );
          return (
            <InteractiveObject
              key={obj.id}
              id={obj.id}
              emoji={obj.emoji}
              label={obj.label}
              x={obj.x}
              y={obj.y}
              width={obj.width}
              height={obj.height}
              available={hasAvailableActions}
              isSelected={selectedObject === obj.id}
              onClick={handleObjectClick}
            />
          );
        })}
      </div>

      {/* Action menu popup */}
      {selectedObject && menuActions.length > 0 && (() => {
        const obj = ROOM_OBJECTS.find(o => o.id === selectedObject)!;
        return (
          <div style={{
            position: 'absolute',
            left: `${obj.x}%`,
            top: `${obj.y}%`,
          }}>
            <ActionMenu
              actions={menuActions}
              onSelect={handleActionSelect}
              onClose={() => { setSelectedObject(null); setMenuActions([]); }}
              pendingAction={pendingAction}
            />
          </div>
        );
      })()}

      {/* Action feedback */}
      {feedback && (
        <div className={styles.actionFeedback}>{feedback}</div>
      )}

      {/* Character */}
      <div className={styles.character}>
        <span className={styles.characterSprite}>👩</span>
        <span className={styles.characterName}>
          {gameState?.player.name ?? 'Татьяна'}
        </span>
      </div>

      {/* Location badge */}
      <div className={styles.locationBadge}>
        <span className={styles.locationText}>
          {LOCATION_LABELS[location] ?? location}
        </span>
      </div>

      {/* Clock overlay */}
      <div className={styles.clockOverlay}>
        <span className={styles.timeText}>
          {tod === 'morning' ? '🌅' : tod === 'day' ? '☀️' : tod === 'evening' ? '🌇' : '🌙'}
          {' '}{String(hour).padStart(2, '0')}:00
        </span>
      </div>
    </div>
  );
}
