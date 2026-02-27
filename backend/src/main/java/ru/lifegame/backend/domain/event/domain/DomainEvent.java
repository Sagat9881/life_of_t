package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public interface DomainEvent {
    String sessionId();
    Instant timestamp();
    String eventType();
}
