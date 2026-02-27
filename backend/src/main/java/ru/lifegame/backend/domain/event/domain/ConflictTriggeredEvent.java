package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record ConflictTriggeredEvent(String sessionId, String conflictId) implements DomainEvent {
    @Override public Instant timestamp() { return Instant.now(); }
    @Override public String eventType() { return "CONFLICT_TRIGGERED"; }
}
