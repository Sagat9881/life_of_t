package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

/**
 * Fired after a tactic is applied to an active conflict.
 * Carries the outcome so the frontend can display narrative text and result.
 */
public record ConflictTacticAppliedEvent(
        String sessionId,
        String conflictId,
        String tacticCode,
        boolean success,
        String narrative
) implements DomainEvent {
    @Override public Instant timestamp() { return Instant.now(); }
    @Override public String eventType() { return "CONFLICT_TACTIC_APPLIED"; }
}
