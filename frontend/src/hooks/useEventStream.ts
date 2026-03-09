/**
 * React hook for connecting to SSE event stream.
 * Manages connection lifecycle and provides status.
 */
import { useEffect, useState } from 'react';
import { eventStreamService } from '@/services/eventStreamService';
import { EventStreamStatus } from '@/types/domainEvent';

export interface UseEventStreamOptions {
  /** Session ID to connect to */
  sessionId: string;
  
  /** Auto-connect on mount (default: true) */
  autoConnect?: boolean;
}

export function useEventStream({ sessionId, autoConnect = true }: UseEventStreamOptions) {
  const [status, setStatus] = useState<EventStreamStatus>(EventStreamStatus.DISCONNECTED);

  useEffect(() => {
    // Subscribe to status changes
    const unsubscribe = eventStreamService.onStatusChange(setStatus);

    // Auto-connect if enabled
    if (autoConnect) {
      eventStreamService.connect(sessionId);
    }

    // Cleanup on unmount
    return () => {
      unsubscribe();
      eventStreamService.disconnect();
    };
  }, [sessionId, autoConnect]);

  return {
    status,
    isConnected: status === EventStreamStatus.CONNECTED,
    isConnecting: status === EventStreamStatus.CONNECTING,
    isError: status === EventStreamStatus.ERROR,
    connect: () => eventStreamService.connect(sessionId),
    disconnect: () => eventStreamService.disconnect(),
  };
}
