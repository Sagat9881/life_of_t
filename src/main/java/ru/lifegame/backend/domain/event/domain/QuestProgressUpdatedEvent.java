package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record QuestProgressUpdatedEvent(String sessionId, String questCode) implements DomainEvent {
    @Override public Instant timestamp() { return Instant.now(); }
    @Override public String eventType() { return "QUEST_PROGRESS_UPDATED"; }
}
