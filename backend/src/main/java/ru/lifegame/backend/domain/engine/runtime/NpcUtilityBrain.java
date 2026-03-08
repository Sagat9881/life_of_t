package ru.lifegame.backend.domain.engine.runtime;

import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.NpcMood;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NpcUtilityBrain {

    public record ScoredResult(NpcSpec.ActionSpec action, double score) {}

    /**
     * ScoredCandidate used by NpcLifecycleEngine for hourly tick evaluation.
     */
    public record ScoredCandidate(String actionId, double score) {}

    public Optional<ScoredResult> evaluate(NpcInstance npc, int currentHour, int currentDay) {
        return npc.spec().actions().stream()
            .map(action -> new ScoredResult(action, calculateScore(action, npc, currentHour, currentDay)))
            .filter(sr -> sr.score() > 0)
            .max(Comparator.comparingDouble(ScoredResult::score));
    }

    public Optional<ScoredCandidate> evaluate(NpcInstance npc, Map<String, Object> context) {
        int currentHour = context.containsKey("hour") ? (int) context.get("hour") : 12;
        int currentDay = context.containsKey("day") ? (int) context.get("day") : 1;
        return evaluate(npc, currentHour, currentDay)
            .map(sr -> new ScoredCandidate(sr.action().actionId(), sr.score()));
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
            case "mood" -> evaluateMoodCondition(cond, npc.mood());
            case "schedule" -> "available".equals(cond.value()) || isInSchedule(npc, currentHour);
            case "memory" -> evaluateMemoryCondition(cond, npc);
            case "day" -> evaluateDayCondition(cond, currentDay);
            default -> true;
        };
    }

    private boolean evaluateMoodCondition(NpcSpec.ConditionSpec cond, NpcMood mood) {
        int val = mood.getAxis(cond.target());
        int threshold = Integer.parseInt(cond.value());
        return switch (cond.operator()) {
            case "gte" -> val >= threshold;
            case "lte" -> val <= threshold;
            case "gt" -> val > threshold;
            case "lt" -> val < threshold;
            case "eq" -> val == threshold;
            default -> true;
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
            case "being_ignored" -> npc.memory().isBeingIgnored(3);
            default -> false;
        };
    }

    private boolean evaluateDayCondition(NpcSpec.ConditionSpec cond, int currentDay) {
        int threshold = Integer.parseInt(cond.value());
        return switch (cond.operator()) {
            case "gte" -> currentDay >= threshold;
            case "lte" -> currentDay <= threshold;
            default -> true;
        };
    }

    private double conditionBonus(NpcSpec.ConditionSpec cond, NpcMood mood) {
        if ("mood".equals(cond.type())) {
            return mood.getAxis(cond.target()) / 100.0 * 0.3;
        }
        return 0.0;
    }
}
