package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record EventTriggeredEvent(String sessionId, String eventId) implements DomainEvent {
    @Override public Instant timestamp() { return Instant.now(); }
    @Override public String eventType() { return "EVENT_TRIGGERED"; }
}
