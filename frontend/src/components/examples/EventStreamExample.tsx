/**
 * Example component showing event stream integration.
 * Copy patterns from here for your game UI components.
 */
import { useState, useCallback } from 'react';
import { useEventStream } from '@/hooks/useEventStream';
import { useEventHandler, useGlobalEventHandler } from '@/hooks/useEventHandler';
import { EventStreamStatus } from '@/types/domainEvent';
import type { DomainEvent } from '@/types/domainEvent';

interface EventStreamExampleProps {
  sessionId: string;
}

export function EventStreamExample({ sessionId }: EventStreamExampleProps) {
  const { status, isConnected } = useEventStream({ sessionId });
  const [events, setEvents] = useState<DomainEvent[]>([]);
  const [lastAction, setLastAction] = useState<string | null>(null);

  // Handle specific event type
  useEventHandler('ACTION_EXECUTED', useCallback((event) => {
    console.log('Action executed:', event);
    setLastAction(event.actionCode as string);
  }, []));

  // Handle another event type
  useEventHandler('NPC_ACTIVITY_CHANGED', useCallback((event) => {
    console.log('NPC activity changed:', event.npcId, event.newActivity);
    // Update NPC state in your game
  }, []));

  // Global handler - receives ALL events (for logging/debugging)
  useGlobalEventHandler(useCallback((event) => {
    setEvents(prev => [event, ...prev].slice(0, 10)); // Keep last 10
  }, []));

  return (
    <div style={{ padding: '20px', fontFamily: 'monospace' }}>
      <h2>Event Stream Example</h2>
      
      <div style={{ marginBottom: '20px' }}>
        <strong>Connection Status:</strong> 
        <span style={{ 
          color: status === EventStreamStatus.CONNECTED ? 'green' : 'red',
          marginLeft: '10px'
        }}>
          {status}
        </span>
      </div>

      {lastAction && (
        <div style={{ marginBottom: '20px', padding: '10px', background: '#f0f0f0' }}>
          <strong>Last Action:</strong> {lastAction}
        </div>
      )}

      <div>
        <h3>Recent Events (Last 10)</h3>
        {events.length === 0 && <p>No events yet...</p>}
        
        {events.map((event, idx) => (
          <div 
            key={`${event.eventType}-${event.timestamp}-${idx}`}
            style={{ 
              marginBottom: '10px', 
              padding: '10px', 
              border: '1px solid #ccc',
              borderRadius: '4px',
              background: '#fafafa'
            }}
          >
            <div><strong>{event.eventType}</strong></div>
            <div style={{ fontSize: '0.85em', color: '#666' }}>
              {new Date(event.timestamp).toLocaleTimeString()}
            </div>
            <pre style={{ fontSize: '0.8em', marginTop: '5px' }}>
              {JSON.stringify(event, null, 2)}
            </pre>
          </div>
        ))}
      </div>
    </div>
  );
}

export default EventStreamExample;
