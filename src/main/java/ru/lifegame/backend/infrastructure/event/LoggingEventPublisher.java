package ru.lifegame.backend.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.backend.application.port.out.EventPublisher;
import ru.lifegame.backend.domain.event.DomainEvent;

/**
 * Simple logging implementation of EventPublisher.
 * In production, this could be replaced with event bus, message queue, or event store integration.
 */
public class LoggingEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingEventPublisher.class);

    @Override
    public void publish(DomainEvent event) {
        log.info("Domain event published: type={}, sessionId={}",
                event.getClass().getSimpleName(),
                event.sessionId());
        log.debug("Event details: {}", event);
    }
}
