package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record ActionExecutedEvent(String sessionId, String actionCode) implements DomainEvent {
    @Override public Instant timestamp() { return Instant.now(); }
    @Override public String eventType() { return "ACTION_EXECUTED"; }
}
