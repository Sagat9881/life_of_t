package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record DayEndedEvent(String sessionId, int day) implements DomainEvent {
    @Override public Instant timestamp() { return Instant.now(); }
    @Override public String eventType() { return "DAY_ENDED"; }
}
