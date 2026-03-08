package com.sagat9881.lifeoft.domain.npc.spec;

import java.util.List;

/**
 * An action candidate for NPC Utility AI evaluation.
 * Loaded from XML <action> elements inside NPC spec.
 * The brain scores each action and picks the highest.
 *
 * @param actionId unique action identifier (e.g., "dinner_invite", "phone_call")
 * @param baseScore base utility score before mood/memory multipliers (0.0-1.0)
 * @param eventType GameEventType string if this action generates a player event
 * @param description optional description text for the event
 * @param animation animation key for frontend rendering
 * @param location location where this action takes place
 * @param tags tags for mood multiplier calculation ("social", "positive", "active")
 * @param conditions list of conditions that must ALL be met for this action
 * @param options player-facing options if this action triggers an event
 */
public record ScoredAction(
        String actionId,
        double baseScore,
        String eventType,
        String description,
        String animation,
        String location,
        List<String> tags,
        List<ConditionSpec> conditions,
        List<ActionOption> options
) {
    public ScoredAction {
        if (actionId == null || actionId.isBlank()) throw new IllegalArgumentException("Action id required");
        if (tags == null) tags = List.of();
        if (conditions == null) conditions = List.of();
        if (options == null) options = List.of();
    }

    public boolean hasOptions() {
        return options != null && !options.isEmpty();
    }

    /**
     * A player-facing option for an NPC-initiated event.
     * All values from XML.
     */
    public record ActionOption(
            String id,
            String text,
            String resultText,
            int energyDelta,
            int stressDelta,
            int moodDelta,
            int moneyDelta,
            String relationshipTarget,
            int relationshipDelta
    ) {
        public ActionOption {
            if (id == null || id.isBlank()) throw new IllegalArgumentException("Option id required");
        }
    }
}
