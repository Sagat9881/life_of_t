package ru.lifegame.backend.domain.npc;

import ru.lifegame.backend.domain.npc.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.runtime.NpcInstance;
import ru.lifegame.backend.domain.npc.runtime.NpcMood;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

public class NpcUtilityBrain {

    public record ScoredResult(NpcSpec.ActionSpec action, double score) {}

    /**
     * ScoredCandidate used by NpcLifecycleEngine for hourly tick evaluation.
     * Carries animationKey and locationId resolved from ActionSpec so that
     * NpcLifecycleEngine does not need to re-look up the spec.
     */
    public record ScoredCandidate(String actionId, String animationKey, String locationId, double score) {}

    public Optional<ScoredResult> evaluate(NpcInstance npc, int currentHour, int currentDay) {
        return npc.spec().actions().stream()
            .map(action -> new ScoredResult(action, calculateScore(action, npc, currentHour, currentDay)))
            .filter(sr -> sr.score() > 0)
            .max(Comparator.comparingDouble(ScoredResult::score));
    }

    public Optional<ScoredCandidate> evaluate(NpcInstance npc, Map<String, Object> context) {
        int currentHour = context.containsKey("hour") ? (int) context.get("hour") : 12;
        int currentDay  = context.containsKey("day")  ? (int) context.get("day")  : 1;
        return evaluate(npc, currentHour, currentDay)
            .map(sr -> {
                NpcSpec.ActionSpec action = sr.action();
                // Fall back to safe defaults if XML parser did not populate these fields
                String animKey  = (action.animationKey() != null && !action.animationKey().isBlank())
                        ? action.animationKey()
                        : action.actionId() + "_anim";
                String locId    = (action.locationId() != null && !action.locationId().isBlank())
                        ? action.locationId()
                        : "default";
                return new ScoredCandidate(action.actionId(), animKey, locId, sr.score());
            });
    }

    private double calculateScore(NpcSpec.ActionSpec action, NpcInstance npc, int currentHour, int currentDay) {
        double score = action.baseScore();
        NpcMood mood = npc.mood();

        for (NpcSpec.ConditionSpec cond : action.conditions()) {
            if (!evaluateCondition(cond, npc, currentHour, currentDay)) {
                return 0.0;
            }
            score += conditionBonus(cond, mood);
        }
        return score;
    }

    private boolean evaluateCondition(NpcSpec.ConditionSpec cond, NpcInstance npc, int currentHour, int currentDay) {
        return switch (cond.type()) {
            case "mood"     -> evaluateMoodCondition(cond, npc.mood());
            case "schedule" -> "available".equals(cond.value()) || isInSchedule(npc, currentHour);
            case "memory"   -> evaluateMemoryCondition(cond, npc);
            case "day"      -> evaluateDayCondition(cond, currentDay);
            default         -> true;
        };
    }

    private boolean evaluateMoodCondition(NpcSpec.ConditionSpec cond, NpcMood mood) {
        int val       = resolveAxisValue(mood, cond.target());
        int threshold = Integer.parseInt(cond.value());
        return switch (cond.operator()) {
            case "gte" -> val >= threshold;
            case "lte" -> val <= threshold;
            case "gt"  -> val >  threshold;
            case "lt"  -> val <  threshold;
            case "eq"  -> val == threshold;
            default    -> true;
        };
    }

    private boolean isInSchedule(NpcInstance npc, int currentHour) {
        return npc.spec().schedule().stream()
            .anyMatch(s -> currentHour >= s.start() && currentHour < s.end());
    }

    private boolean evaluateMemoryCondition(NpcSpec.ConditionSpec cond, NpcInstance npc) {
        if (!npc.spec().memoryEnabled()) return false;
        return switch (cond.target()) {
            case "work_obsession" -> npc.memory().detectPattern("GO_TO_WORK", 3);
            case "being_ignored"  -> npc.memory().isBeingIgnored("DATE_WITH_HUSBAND", 3);
            default               -> false;
        };
    }

    private boolean evaluateDayCondition(NpcSpec.ConditionSpec cond, int currentDay) {
        int threshold = Integer.parseInt(cond.value());
        return switch (cond.operator()) {
            case "gte" -> currentDay >= threshold;
            case "lte" -> currentDay <= threshold;
            default    -> true;
        };
    }

    private double conditionBonus(NpcSpec.ConditionSpec cond, NpcMood mood) {
        if ("mood".equals(cond.type())) {
            return resolveAxisValue(mood, cond.target()) / 100.0 * 0.3;
        }
        return 0.0;
    }

    /**
     * Resolves the integer value of the named mood axis.
     * NpcMood exposes individual getters — there is no generic getAxis(String) method.
     */
    private static int resolveAxisValue(NpcMood mood, String axis) {
        return switch (axis) {
            case "happiness" -> mood.happiness();
            case "energy"    -> mood.energy();
            case "stress"    -> mood.stress();
            case "trust"     -> mood.trust();
            case "romance"   -> mood.romance();
            case "anger"     -> mood.anger();
            default          -> 0;
        };
    }
}
