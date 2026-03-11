package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record ConflictResolvedEvent(
        String sessionId,
        String conflictId,
        String outcome,
        Instant timestamp
) implements DomainEvent {

    public ConflictResolvedEvent(String sessionId, String conflictId, String outcome) {
        this(sessionId, conflictId, outcome, Instant.now());
    }

    @Override public String eventType() { return "CONFLICT_RESOLVED"; }
}
