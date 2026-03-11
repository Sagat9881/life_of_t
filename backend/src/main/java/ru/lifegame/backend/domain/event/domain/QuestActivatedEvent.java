package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record QuestActivatedEvent(
        String sessionId,
        String questId,
        String questTitle,
        Instant timestamp
) implements DomainEvent {

    public QuestActivatedEvent(String sessionId, String questId, String questTitle) {
        this(sessionId, questId, questTitle, Instant.now());
    }

    @Override public String eventType() { return "QUEST_ACTIVATED"; }
}
