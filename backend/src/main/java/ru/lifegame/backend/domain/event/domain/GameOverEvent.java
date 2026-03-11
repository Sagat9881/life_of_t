package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record GameOverEvent(
        String sessionId,
        String reason,
        Instant timestamp
) implements DomainEvent {

    public GameOverEvent(String sessionId, String reason) {
        this(sessionId, reason, Instant.now());
    }

    @Override public String eventType() { return "GAME_OVER"; }
}
