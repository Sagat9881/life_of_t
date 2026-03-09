/**
 * Service for managing Server-Sent Events (SSE) connection.
 * Handles connection lifecycle, reconnection, and event dispatching.
 */
import type { DomainEvent, DomainEventHandler } from '@/types/domainEvent';
import { EventStreamStatus } from '@/types/domainEvent';

type StatusChangeCallback = (status: EventStreamStatus) => void;

export class EventStreamService {
  private eventSource: EventSource | null = null;
  private sessionId: string | null = null;
  private handlers = new Map<string, Set<DomainEventHandler>>();
  private globalHandlers = new Set<DomainEventHandler>();
  private statusCallbacks = new Set<StatusChangeCallback>();
  private reconnectAttempts = 0;
  private readonly maxReconnectAttempts = 5;
  private readonly reconnectDelay = 3000;
  private reconnectTimeoutId: number | null = null;

  /**
   * Connect to SSE stream for a session.
   */
  connect(sessionId: string): void {
    if (this.eventSource?.readyState === EventSource.OPEN) {
      console.warn('[EventStream] Already connected');
      return;
    }

    this.sessionId = sessionId;
    this.updateStatus(EventStreamStatus.CONNECTING);
    
    const url = `/api/events/stream/${sessionId}`;
    console.log(`[EventStream] Connecting to ${url}`);

    try {
      this.eventSource = new EventSource(url);

      this.eventSource.onopen = () => {
        console.log('[EventStream] Connected');
        this.reconnectAttempts = 0;
        this.updateStatus(EventStreamStatus.CONNECTED);
      };

      this.eventSource.onerror = (error) => {
        console.error('[EventStream] Connection error:', error);
        this.updateStatus(EventStreamStatus.ERROR);
        this.handleReconnect();
      };

      // Listen to 'connected' confirmation event from backend
      this.eventSource.addEventListener('connected', (e) => {
        console.log('[EventStream] Received connection confirmation:', e.data);
      });

      // Setup wildcard listener for all event types
      // SSE sends events with type = eventType from backend
      // We need to register listener for each possible eventType,
      // but we don't know them in advance. Solution: use message event
      // which catches all events without explicit type.
      this.eventSource.onmessage = (e) => {
        this.handleIncomingEvent(e);
      };

      // Also listen to typed events (when backend sends event.name(type))
      this.setupTypedEventListeners();
      
    } catch (error) {
      console.error('[EventStream] Failed to create EventSource:', error);
      this.updateStatus(EventStreamStatus.ERROR);
    }
  }

  /**
   * Setup listeners for all registered event types.
   * This handles events sent with explicit event.name() in SSE.
   */
  private setupTypedEventListeners(): void {
    if (!this.eventSource) return;

    // Listen to all registered event types
    for (const eventType of this.handlers.keys()) {
      this.eventSource.addEventListener(eventType, (e) => {
        this.handleIncomingEvent(e);
      });
    }
  }

  /**
   * Handle incoming SSE event.
   */
  private handleIncomingEvent(e: MessageEvent): void {
    try {
      const event: DomainEvent = JSON.parse(e.data);
      
      console.log(`[EventStream] Received event: ${event.eventType}`, event);

      // Call global handlers (receive ALL events)
      for (const handler of this.globalHandlers) {
        try {
          handler(event);
        } catch (error) {
          console.error(`[EventStream] Global handler error:`, error);
        }
      }

      // Call type-specific handlers
      const typeHandlers = this.handlers.get(event.eventType);
      if (typeHandlers) {
        for (const handler of typeHandlers) {
          try {
            handler(event);
          } catch (error) {
            console.error(`[EventStream] Handler error for ${event.eventType}:`, error);
          }
        }
      }
    } catch (error) {
      console.error('[EventStream] Failed to parse event:', error);
    }
  }

  /**
   * Register a handler for specific event type.
   */
  on(eventType: string, handler: DomainEventHandler): () => void {
    if (!this.handlers.has(eventType)) {
      this.handlers.set(eventType, new Set());
      
      // If already connected, add listener for this new type
      if (this.eventSource) {
        this.eventSource.addEventListener(eventType, (e) => {
          this.handleIncomingEvent(e);
        });
      }
    }

    this.handlers.get(eventType)!.add(handler);

    // Return unsubscribe function
    return () => {
      const handlers = this.handlers.get(eventType);
      if (handlers) {
        handlers.delete(handler);
        if (handlers.size === 0) {
          this.handlers.delete(eventType);
        }
      }
    };
  }

  /**
   * Register a global handler that receives ALL events.
   */
  onAny(handler: DomainEventHandler): () => void {
    this.globalHandlers.add(handler);
    return () => this.globalHandlers.delete(handler);
  }

  /**
   * Register a callback for connection status changes.
   */
  onStatusChange(callback: StatusChangeCallback): () => void {
    this.statusCallbacks.add(callback);
    return () => this.statusCallbacks.delete(callback);
  }

  /**
   * Disconnect from SSE stream.
   */
  disconnect(): void {
    console.log('[EventStream] Disconnecting');
    
    if (this.reconnectTimeoutId !== null) {
      clearTimeout(this.reconnectTimeoutId);
      this.reconnectTimeoutId = null;
    }

    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }

    this.updateStatus(EventStreamStatus.DISCONNECTED);
  }

  /**
   * Get current connection status.
   */
  getStatus(): EventStreamStatus {
    if (!this.eventSource) return EventStreamStatus.DISCONNECTED;
    
    switch (this.eventSource.readyState) {
      case EventSource.CONNECTING:
        return EventStreamStatus.CONNECTING;
      case EventSource.OPEN:
        return EventStreamStatus.CONNECTED;
      case EventSource.CLOSED:
        return EventStreamStatus.DISCONNECTED;
      default:
        return EventStreamStatus.ERROR;
    }
  }

  private updateStatus(status: EventStreamStatus): void {
    for (const callback of this.statusCallbacks) {
      try {
        callback(status);
      } catch (error) {
        console.error('[EventStream] Status callback error:', error);
      }
    }
  }

  private handleReconnect(): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('[EventStream] Max reconnect attempts reached');
      return;
    }

    this.reconnectAttempts++;
    const delay = this.reconnectDelay * this.reconnectAttempts;
    
    console.log(`[EventStream] Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts})`);
    
    this.reconnectTimeoutId = window.setTimeout(() => {
      if (this.sessionId) {
        this.disconnect();
        this.connect(this.sessionId);
      }
    }, delay);
  }
}

// Singleton instance
export const eventStreamService = new EventStreamService();
