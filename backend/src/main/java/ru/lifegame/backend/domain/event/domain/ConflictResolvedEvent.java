package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record ConflictResolvedEvent(String sessionId, String conflictId, String outcome) implements DomainEvent {
    @Override public Instant timestamp() { return Instant.now(); }
    @Override public String eventType() { return "CONFLICT_RESOLVED"; }
}
