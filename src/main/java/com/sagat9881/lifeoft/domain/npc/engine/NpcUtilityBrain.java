package com.sagat9881.lifeoft.domain.npc.engine;

import com.sagat9881.lifeoft.domain.npc.model.NpcInstance;
import com.sagat9881.lifeoft.domain.npc.model.NpcActivity;
import com.sagat9881.lifeoft.domain.npc.spec.ScoredAction;
import com.sagat9881.lifeoft.domain.npc.spec.ConditionSpec;
import com.sagat9881.lifeoft.domain.model.session.GameSessionContext;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Utility AI brain for NPC decision-making.
 * Evaluates all available actions from NPC spec via scoring functions.
 * No hardcoded NPC names or action IDs — purely data-driven from XML.
 */
public class NpcUtilityBrain {

    private final ConditionEvaluator conditionEvaluator;

    public NpcUtilityBrain(ConditionEvaluator conditionEvaluator) {
        this.conditionEvaluator = conditionEvaluator;
    }

    /**
     * Evaluate all scored actions for an NPC and return the best one.
     * Each action's final score = baseScore * moodMultiplier * conditionBonus.
     * Actions whose conditions are not met are filtered out.
     */
    public Optional<NpcActivity> selectBestAction(NpcInstance npc, GameSessionContext context) {
        List<ScoredAction> availableActions = npc.spec().actions();
        if (availableActions == null || availableActions.isEmpty()) {
            return selectScheduleDefault(npc, context);
        }

        return availableActions.stream()
                .filter(action -> allConditionsMet(action.conditions(), npc, context))
                .map(action -> new ActionScore(action, computeScore(action, npc, context)))
                .max(Comparator.comparingDouble(ActionScore::score))
                .filter(as -> as.score() > getScheduleThreshold(npc, context))
                .map(as -> toActivity(as.action(), npc));
    }

    private boolean allConditionsMet(List<ConditionSpec> conditions, NpcInstance npc, GameSessionContext context) {
        if (conditions == null || conditions.isEmpty()) return true;
        return conditions.stream().allMatch(c -> conditionEvaluator.evaluate(c, npc, context));
    }

    private double computeScore(ScoredAction action, NpcInstance npc, GameSessionContext context) {
        double base = action.baseScore();
        double moodMultiplier = computeMoodMultiplier(action, npc);
        double memoryMultiplier = computeMemoryMultiplier(action, npc);
        return base * moodMultiplier * memoryMultiplier;
    }

    private double computeMoodMultiplier(ScoredAction action, NpcInstance npc) {
        var mood = npc.mood();
        double multiplier = 1.0;

        // High loneliness boosts social actions
        if (action.tags() != null && action.tags().contains("social")) {
            multiplier += mood.loneliness() / 100.0 * 0.5;
        }
        // High irritability suppresses positive actions
        if (action.tags() != null && action.tags().contains("positive")) {
            multiplier -= mood.irritability() / 100.0 * 0.3;
        }
        // Low energy suppresses active actions
        if (action.tags() != null && action.tags().contains("active")) {
            multiplier *= Math.max(0.3, mood.energy() / 100.0);
        }

        return Math.max(0.1, multiplier);
    }

    private double computeMemoryMultiplier(ScoredAction action, NpcInstance npc) {
        if (npc.memory() == null) return 1.0;

        // If NPC recently did this action, reduce score (variety)
        long recentCount = npc.memory().shortTermEntries().stream()
                .filter(e -> e.eventId().equals(action.actionId()))
                .count();
        if (recentCount > 0) {
            return Math.max(0.3, 1.0 - recentCount * 0.2);
        }
        return 1.0;
    }

    private double getScheduleThreshold(NpcInstance npc, GameSessionContext context) {
        // Only override schedule if action score exceeds threshold
        // Extreme moods lower the threshold (easier to override)
        double base = 0.5;
        var mood = npc.mood();
        if (mood.loneliness() > 70 || mood.irritability() > 70 || mood.anxiety() > 70) {
            base = 0.3;
        }
        return base;
    }

    private Optional<NpcActivity> selectScheduleDefault(NpcInstance npc, GameSessionContext context) {
        int currentHour = context.time().hour();
        return npc.schedule().getSlotForHour(currentHour)
                .map(slot -> new NpcActivity(slot.activity(), slot.animation(), slot.location()));
    }

    private NpcActivity toActivity(ScoredAction action, NpcInstance npc) {
        String animation = action.animation() != null ? action.animation() : "idle";
        String location = action.location() != null ? action.location() : npc.currentLocation();
        return new NpcActivity(action.actionId(), animation, location);
    }

    private record ActionScore(ScoredAction action, double score) {}
}
