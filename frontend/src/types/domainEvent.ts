/**
 * Generic domain event from backend.
 * All events have these base fields + arbitrary additional fields.
 * Frontend doesn't need to know event structure in advance - 
 * just handle them dynamically based on eventType.
 */
export interface DomainEvent {
  /** Event type identifier (e.g., 'ACTION_EXECUTED', 'NPC_ACTIVITY_CHANGED') */
  eventType: string;
  
  /** Unix timestamp in milliseconds */
  timestamp: number;
  
  /** Session ID this event belongs to */
  sessionId: string;
  
  /** All other fields are dynamic - access via event[key] */
  [key: string]: unknown;
}

/**
 * Event handler function type.
 * Receives the full event payload and can do whatever it needs.
 */
export type DomainEventHandler = (event: DomainEvent) => void | Promise<void>;

/**
 * Connection status for SSE stream.
 */
export enum EventStreamStatus {
  DISCONNECTED = 'DISCONNECTED',
  CONNECTING = 'CONNECTING',
  CONNECTED = 'CONNECTED',
  ERROR = 'ERROR',
}
