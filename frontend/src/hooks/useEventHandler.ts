/**
 * useEventHandler Hook
 * 
 * Subscribes to domain events from backend SSE stream.
 * Useful for reactive UI updates (e.g., stat changes, quest completions).
 */

import { useEffect } from 'react';
import { useEventStream } from './useEventStream';

type EventHandler = (event: { type: string; data: unknown }) => void;

export function useEventHandler(eventType: string, handler: EventHandler) {
  const events = useEventStream();

  useEffect(() => {
    const matching = events.filter(e => e.type === eventType);
    matching.forEach(handler);
  }, [events, eventType, handler]);
}
