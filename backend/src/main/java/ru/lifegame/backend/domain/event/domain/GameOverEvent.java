package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record GameOverEvent(String sessionId, String reason) implements DomainEvent {
    @Override public Instant timestamp() { return Instant.now(); }
    @Override public String eventType() { return "GAME_OVER"; }
}
