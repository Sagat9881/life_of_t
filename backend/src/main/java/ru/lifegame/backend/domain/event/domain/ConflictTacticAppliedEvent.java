package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record ConflictTacticAppliedEvent(String sessionId, String conflictId, String tacticCode) implements DomainEvent {
    @Override public Instant timestamp() { return Instant.now(); }
    @Override public String eventType() { return "CONFLICT_TACTIC_APPLIED"; }
}
