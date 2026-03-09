package ru.lifegame.backend.domain.conflict.spec;

import java.util.Map;

/**
 * Outcome when tactic succeeds.
 */
public record SuccessOutcome(
        Map<String, Integer> statChanges,          // "stress" -> -10
        Map<String, Integer> relationshipChanges,  // "HUSBAND.closeness" -> +5
        String narrative                           // optional flavor text
) {
}
