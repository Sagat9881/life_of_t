package ru.lifegame.backend.domain.event.domain;

import ru.lifegame.backend.domain.narrative.spec.QuestSpec.RewardSpec;

import java.time.Instant;
import java.util.List;

public record QuestStepCompletedEvent(
        String sessionId,
        String questId,
        String stepId,
        boolean questCompleted,
        List<RewardSpec> rewards
) implements DomainEvent {

    public QuestStepCompletedEvent(String sessionId, String questId,
                                   String stepId, boolean questCompleted) {
        this(sessionId, questId, stepId, questCompleted, List.of());
    }

    @Override public Instant timestamp() { return Instant.now(); }
    @Override public String eventType()  { return "QUEST_STEP_COMPLETED"; }
}
