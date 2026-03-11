package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record DayEndedEvent(
        String sessionId,
        int day,
        Instant timestamp
) implements DomainEvent {

    public DayEndedEvent(String sessionId, int day) {
        this(sessionId, day, Instant.now());
    }

    @Override public String eventType() { return "DAY_ENDED"; }
}
