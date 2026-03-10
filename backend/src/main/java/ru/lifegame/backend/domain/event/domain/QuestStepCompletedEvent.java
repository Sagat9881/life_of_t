package ru.lifegame.backend.domain.event.domain;

import ru.lifegame.backend.domain.narrative.spec.QuestSpec.RewardSpec;

import java.util.List;

/**
 * Published when a quest step is completed.
 * Carries the step’s rewards so the frontend can animate reward pop-ups
 * (e.g. "+15 mood", "+skill: communication").
 *
 * @param sessionId      the game session
 * @param questId        quest id from QuestSpec
 * @param stepId         completed step id
 * @param questCompleted true if this was the final step
 * @param rewards        rewards granted; may be empty
 */
public record QuestStepCompletedEvent(
        String sessionId,
        String questId,
        String stepId,
        boolean questCompleted,
        List<RewardSpec> rewards
) implements DomainEvent {

    /** Backward-compat constructor (no rewards) — used in tests. */
    public QuestStepCompletedEvent(String sessionId, String questId,
                                   String stepId, boolean questCompleted) {
        this(sessionId, questId, stepId, questCompleted, List.of());
    }

    @Override
    public String type() {
        return "QUEST_STEP_COMPLETED";
    }
}
