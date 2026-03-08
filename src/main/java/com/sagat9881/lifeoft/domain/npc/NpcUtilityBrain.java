package com.sagat9881.lifeoft.domain.npc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Utility AI brain for NPC decision-making.
 * Evaluates all available scored actions from the NPC spec,
 * checks conditions against current context, and selects
 * the highest-scoring action. No hardcoded behavior —
 * all actions come from XML specifications.
 */
public class NpcUtilityBrain {

    private final ConditionEvaluator conditionEvaluator;

    public NpcUtilityBrain(ConditionEvaluator conditionEvaluator) {
        this.conditionEvaluator = conditionEvaluator;
    }

    /**
     * Evaluate all available actions for an NPC and return the best one.
     * Actions whose conditions are not met are filtered out.
     * Score is modified by mood and personality weights.
     */
    public Optional<ScoredAction> evaluate(NpcInstance npc, Object gameContext) {
        List<ScoredAction> availableActions = npc.spec().actions();
        if (availableActions == null || availableActions.isEmpty()) {
            return Optional.empty();
        }

        List<EvaluatedAction> evaluated = new ArrayList<>();

        for (ScoredAction action : availableActions) {
            if (allConditionsMet(action, npc, gameContext)) {
                double finalScore = calculateScore(action, npc);
                evaluated.add(new EvaluatedAction(action, finalScore));
            }
        }

        return evaluated.stream()
                .max(Comparator.comparingDouble(EvaluatedAction::score))
                .map(EvaluatedAction::action);
    }

    private boolean allConditionsMet(ScoredAction action, NpcInstance npc, Object gameContext) {
        if (action.conditions() == null || action.conditions().isEmpty()) {
            return true;
        }
        return action.conditions().stream()
                .allMatch(cond -> conditionEvaluator.evaluate(cond, npc, gameContext));
    }

    /**
     * Calculate final score by applying mood-based modifiers to base score.
     * High loneliness boosts social actions, high irritability suppresses them.
     * Personality traits from spec further weight the score.
     */
    private double calculateScore(ScoredAction action, NpcInstance npc) {
        double score = action.baseScore();
        NpcMood mood = npc.mood();

        // Mood axis modifiers — emergent behavior from mood state
        if (action.actionId().contains("invite") || action.actionId().contains("call")) {
            score += mood.loneliness() * 0.01;
            score -= mood.irritability() * 0.005;
            score += mood.affection() * 0.008;
        }
        if (action.actionId().contains("concern") || action.actionId().contains("worry")) {
            score += mood.anxiety() * 0.01;
        }
        if (action.actionId().contains("criticism") || action.actionId().contains("anger")) {
            score += mood.irritability() * 0.015;
            score -= mood.happiness() * 0.005;
        }

        // Personality trait weights from spec
        var traits = npc.spec().personalityTraits();
        if (traits != null) {
            Integer warmth = traits.get("warmth");
            if (warmth != null && (action.actionId().contains("invite") || action.actionId().contains("advice"))) {
                score *= (1.0 + warmth / 200.0);
            }
            Integer patience = traits.get("patience");
            if (patience != null && action.actionId().contains("criticism")) {
                score *= (1.0 - patience / 200.0);
            }
        }

        // Energy gate — exhausted NPCs don't initiate
        if (mood.energy() < 20) {
            score *= 0.3;
        }

        return Math.max(0, score);
    }

    private record EvaluatedAction(ScoredAction action, double score) {}
}
