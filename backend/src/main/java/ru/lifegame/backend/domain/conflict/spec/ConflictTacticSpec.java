package ru.lifegame.backend.domain.conflict.spec;

/**
 * Data-driven tactic specification.
 * Defines outcomes based on context instead of hardcoded logic.
 */
public record ConflictTacticSpec(
        String code,
        String label,
        String description,
        int baseCspCost,           // CSP cost to player
        int baseOpponentCspCost,   // CSP damage to opponent
        SuccessOutcome successOutcome,
        FailureOutcome failureOutcome
) {
}
