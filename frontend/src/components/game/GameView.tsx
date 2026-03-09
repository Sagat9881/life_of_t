/**
 * GameView — Complete example of game UI with event stream integration.
 *
 * Shows how to:
 * - Connect to event stream
 * - Handle domain events (NPC activity, actions, etc.)
 * - Update character animations based on events
 * - Render location with Canvas
 */
import { useState, useCallback, useEffect } from 'react';
import { LocationRenderer } from './LocationRenderer';
import { useEventStream } from '@/hooks/useEventStream';
import { useEventHandler } from '@/hooks/useEventHandler';
import type { LocationConfig } from '@/config/locations';
import { EventStreamStatus } from '@/types/domainEvent';

export interface GameViewProps {
  /** Current location config */
  location: LocationConfig;
  
  /** Session ID for event stream */
  sessionId: string;
  
  /** Current time of day */
  timeOfDay?: string;
  
  /** Callback when user clicks on interactive object */
  onObjectClick?: (objectId: string, actionCode: string) => void;
}

export function GameView({ 
  location, 
  sessionId, 
  timeOfDay = 'day',
  onObjectClick 
}: GameViewProps) {
  const [selectedObjectId, setSelectedObjectId] = useState<string | null>(null);
  const [characterAnimations, setCharacterAnimations] = useState<Record<string, string>>({});
  
  // Connect to event stream
  const { status, isConnected } = useEventStream({ sessionId });

  // Handle NPC activity changes → update character animations
  useEventHandler('NPC_ACTIVITY_CHANGED', useCallback((event) => {
    const npcId = event.npcId as string;
    const newActivity = event.newActivity as string;
    
    console.log(`[GameView] NPC ${npcId} changed activity to ${newActivity}`);
    
    // Map activity to animation name
    // This is game-specific logic - adjust based on your activity system
    const animationMap: Record<string, string> = {
      'WORKING': 'work',
      'SLEEPING': 'sleep',
      'EATING': 'eat',
      'EXERCISING': 'exercise',
      'IDLE': 'idle',
    };
    
    const animation = animationMap[newActivity] || 'idle';
    
    setCharacterAnimations(prev => ({
      ...prev,
      [npcId]: animation,
    }));
  }, []));

  // Handle action execution events
  useEventHandler('ACTION_EXECUTED', useCallback((event) => {
    console.log(`[GameView] Action executed:`, event.actionCode);
    // Could trigger animations, particle effects, sound, etc.
  }, []));

  // Handle narrative events (story events with choices)
  useEventHandler('NARRATIVE_EVENT_TRIGGERED', useCallback((event) => {
    console.log(`[GameView] Narrative event:`, event.title);
    // Show modal with event.title, event.description, event.options
    // This is where you'd integrate your story UI
  }, []));

  const handleObjectClick = useCallback((objectId: string, actionCode: string) => {
    setSelectedObjectId(objectId);
    onObjectClick?.(objectId, actionCode);
  }, [onObjectClick]);

  return (
    <div style={{ width: '100%', height: '100%', position: 'relative' }}>
      {/* Connection status indicator */}
      <ConnectionStatus status={status} />
      
      {/* Main game canvas */}
      <LocationRenderer
        config={location}
        timeOfDay={timeOfDay}
        selectedObjectId={selectedObjectId}
        onObjectClick={handleObjectClick}
        characterAnimations={characterAnimations}
      />
      
      {/* Debug info (remove in production) */}
      {process.env.NODE_ENV === 'development' && (
        <DebugPanel 
          isConnected={isConnected}
          characterAnimations={characterAnimations}
        />
      )}
    </div>
  );
}

/** Connection status indicator */
function ConnectionStatus({ status }: { status: EventStreamStatus }) {
  if (status === EventStreamStatus.CONNECTED) return null;
  
  const colors: Record<EventStreamStatus, string> = {
    [EventStreamStatus.DISCONNECTED]: '#666',
    [EventStreamStatus.CONNECTING]: '#ff9800',
    [EventStreamStatus.CONNECTED]: '#4caf50',
    [EventStreamStatus.ERROR]: '#f44336',
  };
  
  return (
    <div
      style={{
        position: 'absolute',
        top: 10,
        right: 10,
        padding: '8px 12px',
        background: 'rgba(0, 0, 0, 0.7)',
        color: colors[status],
        borderRadius: '4px',
        fontSize: '12px',
        fontFamily: 'monospace',
        zIndex: 1000,
      }}
    >
      {status}
    </div>
  );
}

/** Debug panel showing event-driven state */
function DebugPanel({ 
  isConnected, 
  characterAnimations 
}: { 
  isConnected: boolean;
  characterAnimations: Record<string, string>;
}) {
  return (
    <div
      style={{
        position: 'absolute',
        bottom: 10,
        left: 10,
        padding: '10px',
        background: 'rgba(0, 0, 0, 0.8)',
        color: '#fff',
        borderRadius: '4px',
        fontSize: '11px',
        fontFamily: 'monospace',
        maxWidth: '300px',
        zIndex: 1000,
      }}
    >
      <div style={{ marginBottom: '8px', fontWeight: 'bold' }}>
        🔧 Debug Panel
      </div>
      <div>Events: {isConnected ? '🟢 Connected' : '🔴 Disconnected'}</div>
      <div style={{ marginTop: '8px' }}>Character Animations:</div>
      {Object.entries(characterAnimations).length === 0 ? (
        <div style={{ color: '#888', fontSize: '10px' }}>No overrides</div>
      ) : (
        Object.entries(characterAnimations).map(([npcId, anim]) => (
          <div key={npcId} style={{ fontSize: '10px', color: '#4caf50' }}>
            {npcId}: {anim}
          </div>
        ))
      )}
    </div>
  );
}

export default GameView;
