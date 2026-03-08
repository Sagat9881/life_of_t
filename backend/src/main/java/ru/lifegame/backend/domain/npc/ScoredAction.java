package ru.lifegame.backend.domain.npc;

import java.util.List;
import java.util.Map;

/**
 * An NPC-initiated action candidate loaded from XML spec.
 * Contains conditions (evaluated at runtime) and player-facing options.
 * No hardcoded logic — all behavior is data-driven.
 */
public record ScoredAction(
    String actionId,
    String title,
    String description,
    String eventType,
    double baseScore,
    List<ConditionSpec> conditions,
    List<ActionOption> options
) {
    /**
     * Player-facing option within an NPC-initiated event.
     */
    public record ActionOption(
        String id,
        String text,
        String resultText,
        int energy,
        int stress,
        int mood,
        int money,
        int selfEsteem,
        int jobSatisfaction,
        Map<String, Integer> relationshipDeltas,
        Map<String, Integer> skillDeltas
    ) {
        public static ActionOption simple(String id, String text, String resultText,
                                          Map<String, Integer> relationshipDeltas) {
            return new ActionOption(id, text, resultText, 0, 0, 0, 0, 0, 0,
                relationshipDeltas, Map.of());
        }
    }
}
