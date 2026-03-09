/**
 * useEventHandler Hook
 * 
 * Subscribes to domain events from backend SSE stream.
 * Useful for reactive UI updates (e.g., stat changes, quest completions).
 */

import { useEffect } from 'react';
import { useEventStream } from './useEventStream';

type EventHandler = (event: { type: string; data: unknown }) => void;

export function useEventHandler(sessionId: string, eventType: string, handler: EventHandler) {
  const { status } = useEventStream({ sessionId, autoConnect: true });

  useEffect(() => {
    // TODO: Subscribe to specific event types from eventStreamService
    // This is a placeholder — actual implementation needs event filtering
    console.log(`Subscribed to ${eventType}, stream status: ${status}`);
  }, [status, eventType, handler]);
}
