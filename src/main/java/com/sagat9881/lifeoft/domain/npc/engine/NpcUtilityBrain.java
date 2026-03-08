package com.sagat9881.lifeoft.domain.npc.engine;

import com.sagat9881.lifeoft.domain.npc.model.NpcInstance;
import com.sagat9881.lifeoft.domain.npc.model.ScoredAction;
import com.sagat9881.lifeoft.domain.npc.model.ConditionSpec;
import com.sagat9881.lifeoft.domain.npc.model.NpcActivity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility AI brain for NPC decision-making.
 * Evaluates all available actions through scoring functions
 * and selects the highest-scoring action.
 * 
 * No hardcoded actions — everything comes from NpcSpec XML.
 * Uses ConditionEvaluator to check conditions against game state.
 */
public class NpcUtilityBrain {

    private final ConditionEvaluator conditionEvaluator;
    private final Random random = new Random();
    private static final double NOISE_FACTOR = 0.1;

    public NpcUtilityBrain(ConditionEvaluator conditionEvaluator) {
        this.conditionEvaluator = conditionEvaluator;
    }

    /**
     * Evaluate all scored actions for an NPC and return the best one.
     * Returns Optional.empty() if no action passes conditions.
     */
    public Optional<ScoredAction> evaluate(NpcInstance npc, Object gameContext) {
        List<ScoredAction> availableActions = npc.spec().scoredActions();
        if (availableActions == null || availableActions.isEmpty()) {
            return Optional.empty();
        }

        List<ScoredAction> eligible = availableActions.stream()
                .filter(action -> allConditionsMet(action.conditions(), npc, gameContext))
                .collect(Collectors.toList());

        if (eligible.isEmpty()) {
            return Optional.empty();
        }

        return eligible.stream()
                .max(Comparator.comparingDouble(action -> computeScore(action, npc)));
    }

    /**
     * Compute final score for an action:
     * baseScore * moodMultipliers + small random noise for variety.
     */
    private double computeScore(ScoredAction action, NpcInstance npc) {
        double score = action.baseScore();

        // Apply mood-based multipliers from action spec
        for (var moodWeight : action.moodWeights().entrySet()) {
            String axis = moodWeight.getKey();
            double weight = moodWeight.getValue();
            double moodValue = npc.mood().getAxis(axis) / 100.0;
            score += moodValue * weight;
        }

        // Apply personality trait multipliers
        for (var traitWeight : action.personalityWeights().entrySet()) {
            String trait = traitWeight.getKey();
            double weight = traitWeight.getValue();
            double traitValue = npc.spec().personalityTrait(trait) / 100.0;
            score += traitValue * weight;
        }

        // Add small noise for non-deterministic behavior
        score += (random.nextDouble() - 0.5) * NOISE_FACTOR;

        return Math.max(0.0, score);
    }

    /**
     * Check if all conditions of an action are met.
     */
    private boolean allConditionsMet(List<ConditionSpec> conditions, NpcInstance npc, Object gameContext) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }
        return conditions.stream()
                .allMatch(cond -> conditionEvaluator.evaluate(cond, npc, gameContext));
    }

    /**
     * Select activity for NPC based on schedule + utility override.
     * If utility brain finds a high-scoring action, it overrides schedule.
     * Otherwise, falls back to schedule-based activity.
     */
    public NpcActivity selectActivity(NpcInstance npc, int currentHour, Object gameContext) {
        // First check if any utility action scores above threshold
        Optional<ScoredAction> utilityAction = evaluate(npc, gameContext);
        if (utilityAction.isPresent() && utilityAction.get().baseScore() > 0.7) {
            ScoredAction action = utilityAction.get();
            return new NpcActivity(
                    action.actionId(),
                    action.animationKey(),
                    action.locationId(),
                    1
            );
        }

        // Fall back to schedule
        return npc.getScheduledActivity(currentHour)
                .orElse(NpcActivity.idle(npc.spec().defaultLocation()));
    }
}
