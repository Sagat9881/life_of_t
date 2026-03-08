package com.sagat9881.lifeoft.domain.npc.model;

import java.util.*;

/**
 * An action available to an NPC, with scoring parameters.
 * Loaded from XML — the engine uses this to evaluate via Utility AI.
 * 
 * baseScore: starting score before mood/personality multipliers.
 * conditions: list of ConditionSpec that must all be true.
 * moodWeights: how each mood axis affects the score.
 * personalityWeights: how each personality trait affects the score.
 * options: player response options (if this action generates an event).
 */
public record ScoredAction(
        String actionId,
        double baseScore,
        String animationKey,
        String locationId,
        String eventType,
        boolean isEventInitiator,
        List<ConditionSpec> conditions,
        Map<String, Double> moodWeights,
        Map<String, Double> personalityWeights,
        List<Map<String, String>> options
) {

    public ScoredAction {
        conditions = conditions != null ? List.copyOf(conditions) : List.of();
        moodWeights = moodWeights != null ? Map.copyOf(moodWeights) : Map.of();
        personalityWeights = personalityWeights != null ? Map.copyOf(personalityWeights) : Map.of();
        options = options != null ? List.copyOf(options) : List.of();
    }
}
