package ru.lifegame.backend.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages Server-Sent Events (SSE) emitters for real-time event broadcasting.
 * Each session can have multiple subscribers (e.g., multiple browser tabs).
 */
@Service
public class EventBroadcaster {
    private static final Logger log = LoggerFactory.getLogger(EventBroadcaster.class);
    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L; // 30 minutes

    // sessionId -> list of active emitters for that session
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> sessionEmitters = new ConcurrentHashMap<>();

    /**
     * Subscribe to events for a given sessionId.
     * Returns an SseEmitter that will receive all events for this session.
     */
    public SseEmitter subscribe(String sessionId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        
        // Add to session's emitter list
        sessionEmitters.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        
        log.info("New SSE subscription for session: {}. Active connections: {}", 
                 sessionId, sessionEmitters.get(sessionId).size());

        // Cleanup on completion/timeout/error
        emitter.onCompletion(() -> removeEmitter(sessionId, emitter));
        emitter.onTimeout(() -> removeEmitter(sessionId, emitter));
        emitter.onError(ex -> removeEmitter(sessionId, emitter));

        // Send initial connection confirmation
        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data(Map.of("sessionId", sessionId, "timestamp", System.currentTimeMillis())));
        } catch (IOException e) {
            log.warn("Failed to send initial event to session {}: {}", sessionId, e.getMessage());
            removeEmitter(sessionId, emitter);
        }

        return emitter;
    }

    /**
     * Broadcast a generic event to all subscribers of a session.
     * @param sessionId target session
     * @param eventType event type identifier (e.g., "ACTION_EXECUTED")
     * @param payload event data as Map (will be serialized to JSON)
     */
    public void broadcast(String sessionId, String eventType, Map<String, Object> payload) {
        CopyOnWriteArrayList<SseEmitter> emitters = sessionEmitters.get(sessionId);
        if (emitters == null || emitters.isEmpty()) {
            log.debug("No active subscribers for session: {}", sessionId);
            return;
        }

        log.debug("Broadcasting event {} to session {} ({} subscribers)", 
                  eventType, sessionId, emitters.size());

        // Send to all active emitters
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                    .name(eventType)
                    .data(payload));
            } catch (IOException e) {
                log.warn("Failed to send event to subscriber: {}", e.getMessage());
                removeEmitter(sessionId, emitter);
            }
        }
    }

    /**
     * Remove a specific emitter and cleanup if no more subscribers for session.
     */
    private void removeEmitter(String sessionId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = sessionEmitters.get(sessionId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                sessionEmitters.remove(sessionId);
                log.info("Session {} has no more active subscribers, removed from registry", sessionId);
            }
        }
        
        try {
            emitter.complete();
        } catch (Exception e) {
            // Ignore - emitter already dead
        }
    }

    /**
     * Get count of active subscribers for a session (for monitoring).
     */
    public int getSubscriberCount(String sessionId) {
        CopyOnWriteArrayList<SseEmitter> emitters = sessionEmitters.get(sessionId);
        return emitters == null ? 0 : emitters.size();
    }
}
