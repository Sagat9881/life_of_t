package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record QuestStepCompletedEvent(
        String sessionId,
        String questId,
        String stepId,
        boolean questCompleted
) implements DomainEvent {
    @Override public Instant timestamp() { return Instant.now(); }
    @Override public String eventType() { return "QUEST_STEP_COMPLETED"; }
}
