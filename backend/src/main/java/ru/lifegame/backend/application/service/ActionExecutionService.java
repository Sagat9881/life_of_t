package ru.lifegame.backend.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ru.lifegame.backend.domain.event.domain.ActionExecutedEvent;

/**
 * Example service showing how to publish domain events.
 * 
 * IMPORTANT: This is a placeholder/example.
 * Real ActionExecutionService should be implemented based on your action system.
 */
@Service
public class ActionExecutionService {
    private static final Logger log = LoggerFactory.getLogger(ActionExecutionService.class);
    private final ApplicationEventPublisher eventPublisher;

    public ActionExecutionService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Example: Execute an action and publish event.
     * Replace with your actual action execution logic.
     */
    public void executeAction(String sessionId, String actionCode) {
        log.info("Executing action: {} for session: {}", actionCode, sessionId);
        
        // TODO: Real action execution logic
        // - Load game state
        // - Apply action effects
        // - Save state
        // - Calculate stat changes
        
        // Publish event - will be automatically broadcast to frontend via SSE
        eventPublisher.publishEvent(
            new ActionExecutedEvent(sessionId, actionCode)
        );
        
        log.debug("Published ActionExecutedEvent for action: {}", actionCode);
    }
}
