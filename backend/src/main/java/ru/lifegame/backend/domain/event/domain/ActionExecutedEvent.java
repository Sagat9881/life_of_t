package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record ActionExecutedEvent(
        String sessionId,
        String actionCode,
        Instant timestamp
) implements DomainEvent {

    public ActionExecutedEvent(String sessionId, String actionCode) {
        this(sessionId, actionCode, Instant.now());
    }

    @Override public String eventType() { return "ACTION_EXECUTED"; }
}
