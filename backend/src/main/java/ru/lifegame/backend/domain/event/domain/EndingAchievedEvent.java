package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record EndingAchievedEvent(String sessionId, String endingType) implements DomainEvent {
    @Override public Instant timestamp() { return Instant.now(); }
    @Override public String eventType() { return "ENDING_ACHIEVED"; }
}
