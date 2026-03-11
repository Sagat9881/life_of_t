package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record EndingAchievedEvent(
        String sessionId,
        String endingType,
        Instant timestamp
) implements DomainEvent {

    public EndingAchievedEvent(String sessionId, String endingType) {
        this(sessionId, endingType, Instant.now());
    }

    @Override public String eventType() { return "ENDING_ACHIEVED"; }
}
