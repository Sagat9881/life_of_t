package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record ConflictTriggeredEvent(
        String sessionId,
        String conflictId,
        Instant timestamp
) implements DomainEvent {

    public ConflictTriggeredEvent(String sessionId, String conflictId) {
        this(sessionId, conflictId, Instant.now());
    }

    @Override public String eventType() { return "CONFLICT_TRIGGERED"; }
}
