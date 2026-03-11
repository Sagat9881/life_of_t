package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;
import java.util.List;

public record QuestStepCompletedEvent(
        String sessionId,
        String questId,
        String stepId,
        boolean questCompleted,
        List<QuestReward> rewards,
        Instant timestamp
) implements DomainEvent {

    public record QuestReward(String type, String target, int amount) {}

    public QuestStepCompletedEvent(String sessionId, String questId,
                                   String stepId, boolean questCompleted) {
        this(sessionId, questId, stepId, questCompleted, List.of(), Instant.now());
    }

    public QuestStepCompletedEvent(String sessionId, String questId,
                                   String stepId, boolean questCompleted,
                                   List<QuestReward> rewards) {
        this(sessionId, questId, stepId, questCompleted, rewards, Instant.now());
    }

    @Override public String eventType() { return "QUEST_STEP_COMPLETED"; }
}
