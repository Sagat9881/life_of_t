/**
 * GameView Component
 * 
 * Main game screen orchestrating:
 * - Canvas rendering (PixelSceneCanvas)
 * - UI overlay (stats, actions)
 * - Event handling
 */

import React, { useState } from 'react';
import { PixelSceneCanvas } from './PixelSceneCanvas';
import { locations } from '../../config/locations';
import type { LocationConfig } from '../../types/location.types';

interface GameViewProps {
  locationId: string;
  onAction: (actionId: string) => void;
}

export function GameView({ locationId, onAction }: GameViewProps) {
  const [timeOfDay, setTimeOfDay] = useState<string>('day');
  const [selectedObjectId, setSelectedObjectId] = useState<string | null>(null);
  const [hoveredObjectId, setHoveredObjectId] = useState<string | null>(null);

  const config = locations[locationId] as LocationConfig;

  if (!config) {
    return <div className="error">Location not found: {locationId}</div>;
  }

  const handleClick = (objectId: string | null) => {
    if (objectId) {
      const furniture = config.furniture?.find(f => f.id === objectId);
      if (furniture?.actions && furniture.actions.length > 0) {
        // Trigger first action
        onAction(furniture.actions[0]);
      }
    }
    setSelectedObjectId(objectId);
  };

  return (
    <div className="game-view">
      {/* Canvas */}
      <PixelSceneCanvas
        config={config}
        timeOfDay={timeOfDay}
        selectedObjectId={selectedObjectId}
        hoveredObjectId={hoveredObjectId}
        onObjectClick={handleClick}
        onObjectHover={setHoveredObjectId}
      />

      {/* Debug Controls */}
      <div className="debug-controls" style={{
        position: 'absolute',
        top: 10,
        right: 10,
        background: 'rgba(0,0,0,0.7)',
        padding: '10px',
        borderRadius: '5px',
        color: 'white',
        fontSize: '12px',
        display: import.meta.env.DEV ? 'block' : 'none'
      }}>
        <div>
          <label>Time: </label>
          <select value={timeOfDay} onChange={(e) => setTimeOfDay(e.target.value)}>
            <option value="day">Day</option>
            <option value="evening">Evening</option>
            <option value="night">Night</option>
          </select>
        </div>
        <div style={{ marginTop: '10px' }}>
          Selected: {selectedObjectId || 'none'}
        </div>
        <div>
          Hovered: {hoveredObjectId || 'none'}
        </div>
      </div>

      {/* Action Panel */}
      {selectedObjectId && (
        <div className="action-panel" style={{
          position: 'absolute',
          bottom: 20,
          left: '50%',
          transform: 'translateX(-50%)',
          background: 'rgba(0,0,0,0.8)',
          padding: '15px',
          borderRadius: '10px',
          color: 'white'
        }}>
          <h3>{selectedObjectId}</h3>
          {config.furniture?.find(f => f.id === selectedObjectId)?.actions?.map(actionId => (
            <button
              key={actionId}
              onClick={() => onAction(actionId)}
              style={{
                display: 'block',
                marginTop: '10px',
                padding: '8px 15px',
                background: '#4a90e2',
                border: 'none',
                borderRadius: '5px',
                color: 'white',
                cursor: 'pointer'
              }}
            >
              {actionId}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
