/**
 * React hook for registering domain event handlers.
 * Automatically unsubscribes on unmount.
 */
import { useEffect } from 'react';
import { eventStreamService } from '@/services/eventStreamService';
import type { DomainEvent, DomainEventHandler } from '@/types/domainEvent';

/**
 * Register a handler for a specific event type.
 * 
 * @example
 * useEventHandler('ACTION_EXECUTED', (event) => {
 *   console.log('Action:', event.actionCode);
 *   // event has all fields from backend, access dynamically
 * });
 */
export function useEventHandler(eventType: string, handler: DomainEventHandler): void {
  useEffect(() => {
    const unsubscribe = eventStreamService.on(eventType, handler);
    return unsubscribe;
  }, [eventType, handler]);
}

/**
 * Register a global handler that receives ALL events.
 * Useful for logging, analytics, or global state updates.
 * 
 * @example
 * useGlobalEventHandler((event) => {
 *   console.log('Event received:', event.eventType, event);
 *   analytics.track(event.eventType, event);
 * });
 */
export function useGlobalEventHandler(handler: DomainEventHandler): void {
  useEffect(() => {
    const unsubscribe = eventStreamService.onAny(handler);
    return unsubscribe;
  }, [handler]);
}

/**
 * Register multiple handlers at once.
 * 
 * @example
 * useEventHandlers({
 *   ACTION_EXECUTED: (e) => console.log('Action:', e.actionCode),
 *   NPC_ACTIVITY_CHANGED: (e) => console.log('NPC:', e.npcId, e.newActivity),
 * });
 */
export function useEventHandlers(handlers: Record<string, DomainEventHandler>): void {
  useEffect(() => {
    const unsubscribers = Object.entries(handlers).map(([eventType, handler]) =>
      eventStreamService.on(eventType, handler)
    );

    return () => {
      unsubscribers.forEach((unsub) => unsub());
    };
  }, [handlers]);
}
