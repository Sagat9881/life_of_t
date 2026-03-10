package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record QuestActivatedEvent(
        String sessionId,
        String questId,
        String questTitle
) implements DomainEvent {
    @Override public Instant timestamp() { return Instant.now(); }
    @Override public String eventType()  { return "QUEST_ACTIVATED"; }
}
