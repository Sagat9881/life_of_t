package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record ConflictTacticAppliedEvent(
        String sessionId,
        String conflictId,
        String tacticCode,
        boolean success,
        String narrative,
        Instant timestamp
) implements DomainEvent {

    public ConflictTacticAppliedEvent(String sessionId, String conflictId,
                                      String tacticCode, boolean success, String narrative) {
        this(sessionId, conflictId, tacticCode, success, narrative, Instant.now());
    }

    @Override public String eventType() { return "CONFLICT_TACTIC_APPLIED"; }
}
