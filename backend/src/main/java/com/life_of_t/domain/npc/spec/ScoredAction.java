package com.life_of_t.domain.npc.spec;

import java.util.List;
import java.util.Map;

/**
 * An NPC-initiated action with Utility AI scoring.
 * Loaded from XML <action> elements inside <actions>.
 * The engine scores each action and picks the highest —
 * no hardcoded action logic in the backend.
 */
public record ScoredAction(
        String actionId,
        double baseScore,
        String eventType,
        List<ConditionSpec> conditions,
        List<ActionOption> options
) {

    /**
     * A player-facing option when this NPC action triggers as an event.
     */
    public record ActionOption(
            String optionId,
            String text,
            String resultDescription,
            Map<String, Integer> statChanges,
            String relationshipTarget,
            int relationshipDelta
    ) {}
}
