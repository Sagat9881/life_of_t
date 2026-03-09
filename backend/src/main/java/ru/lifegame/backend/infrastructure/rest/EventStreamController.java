package ru.lifegame.backend.infrastructure.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.lifegame.backend.infrastructure.event.EventBroadcaster;

/**
 * REST controller for Server-Sent Events (SSE) stream.
 * Frontend connects to /api/events/stream/{sessionId} to receive real-time events.
 */
@RestController
@RequestMapping("/api/events")
public class EventStreamController {
    private static final Logger log = LoggerFactory.getLogger(EventStreamController.class);
    private final EventBroadcaster eventBroadcaster;

    public EventStreamController(EventBroadcaster eventBroadcaster) {
        this.eventBroadcaster = eventBroadcaster;
    }

    /**
     * Subscribe to event stream for a session.
     * Returns SSE stream that will push events in real-time.
     * 
     * Example frontend usage:
     * const eventSource = new EventSource('/api/events/stream/my-session-id');
     * eventSource.addEventListener('ACTION_EXECUTED', (e) => console.log(JSON.parse(e.data)));
     */
    @GetMapping(value = "/stream/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents(@PathVariable String sessionId) {
        log.info("SSE stream requested for session: {}", sessionId);
        return eventBroadcaster.subscribe(sessionId);
    }

    /**
     * Health check endpoint - returns subscriber count for a session.
     */
    @GetMapping("/stream/{sessionId}/health")
    public SubscriberHealthResponse getStreamHealth(@PathVariable String sessionId) {
        int count = eventBroadcaster.getSubscriberCount(sessionId);
        return new SubscriberHealthResponse(sessionId, count, count > 0);
    }

    record SubscriberHealthResponse(String sessionId, int subscriberCount, boolean connected) {}
}
