import { useState } from 'react';
import { Button } from '../components/shared/Button';
import { BottomNav } from '../components/layout/BottomNav/BottomNav';
import './RoomPage.css';

interface InteractiveObject {
  id: string;
  name: string;
  x: number;
  y: number;
  action: string;
  icon: string;
}

const ROOM_OBJECTS: InteractiveObject[] = [
  { id: 'phone', name: 'Телефон', x: 15, y: 20, action: 'CALL_HUSBAND', icon: '📱' },
  { id: 'bed', name: 'Кровать', x: 70, y: 25, action: 'REST_AT_HOME', icon: '🛌️' },
  { id: 'tv', name: 'Телевизор', x: 85, y: 50, action: 'WATCH_TV', icon: '📺' },
  { id: 'pet', name: 'Гарфилд', x: 30, y: 70, action: 'PLAY_WITH_PET', icon: '🐱' },
];

export function RoomPage() {
  const [selectedObject, setSelectedObject] = useState<string | null>(null);

  const handleObjectClick = (obj: InteractiveObject) => {
    setSelectedObject(obj.id);
    console.log('Action:', obj.action);
    // TODO: executeAction(obj.action)
  };

  return (
    <div className="room-page">
      <div className="room-page__scene">
        <div className="room-page__isometric">
          {/* Character Placeholder */}
          <div className="room-page__character">
            <div className="room-page__character-placeholder">👩</div>
          </div>

          {/* Interactive Objects */}
          {ROOM_OBJECTS.map((obj) => (
            <button
              key={obj.id}
              className={`room-page__object ${
                selectedObject === obj.id ? 'room-page__object--selected' : ''
              }`}
              style={{
                left: `${obj.x}%`,
                top: `${obj.y}%`,
              }}
              onClick={() => handleObjectClick(obj)}
              title={obj.name}
            >
              <span className="room-page__object-icon">{obj.icon}</span>
              <span className="room-page__object-label">{obj.name}</span>
            </button>
          ))}
        </div>
      </div>

      {/* Action Confirmation */}
      {selectedObject && (
        <div className="room-page__action-panel">
          <div className="room-page__action-content">
            <p className="room-page__action-text">
              {ROOM_OBJECTS.find((o) => o.id === selectedObject)?.name}
            </p>
            <div className="room-page__action-buttons">
              <Button
                variant="primary"
                onClick={() => {
                  const obj = ROOM_OBJECTS.find((o) => o.id === selectedObject);
                  console.log('Execute:', obj?.action);
                  alert(`Выполнено: ${obj?.name}`);
                  setSelectedObject(null);
                }}
              >
                Выполнить
              </Button>
              <Button variant="outline" onClick={() => setSelectedObject(null)}>
                Отмена
              </Button>
            </div>
          </div>
        </div>
      )}

      <BottomNav current="room" />
    </div>
  );
}
