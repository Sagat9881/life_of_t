package ru.lifegame.backend.domain.conflict.spec;

import java.util.Map;

/**
 * Outcome when tactic fails.
 */
public record FailureOutcome(
        Map<String, Integer> statChanges,
        Map<String, Integer> relationshipChanges,
        String narrative
) {
}
