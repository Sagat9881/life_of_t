package com.sagat9881.lifeoft.domain.npc.spec;

import java.util.List;

/**
 * An action candidate for Utility AI scoring, loaded from XML.
 * The engine evaluates conditions and computes final score to pick the best action.
 *
 * @param actionId unique action identifier from XML
 * @param baseScore base utility score before condition modifiers (0.0 - 1.0)
 * @param eventType type of GameEvent to generate if this action wins
 * @param conditions list of conditions that must be met AND that modify score
 * @param options player-facing choices when this NPC action triggers an event
 */
public record ScoredAction(
        String actionId,
        double baseScore,
        String eventType,
        List<ConditionSpec> conditions,
        List<OptionSpec> options
) {

    /**
     * Checks if all conditions are satisfiable (non-blocking).
     * Score modifiers are applied separately by the UtilityBrain.
     */
    public boolean hasOptions() {
        return options != null && !options.isEmpty();
    }
}
