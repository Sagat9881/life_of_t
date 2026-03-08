package ru.lifegame.backend.domain.engine.runtime;

import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.engine.spec.ConditionSpec;
import ru.lifegame.backend.domain.npc.engine.NpcMood;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class NpcUtilityBrain {

    private final ConditionEvaluator conditionEvaluator;

    public NpcUtilityBrain(ConditionEvaluator conditionEvaluator) {
        this.conditionEvaluator = conditionEvaluator;
    }

    public record ScoredCandidate(NpcSpec.ActionSpec action, double score) {}

    public Optional<ScoredCandidate> evaluate(NpcInstance npc, Object gameContext) {
        return npc.spec().actions().stream()
            .filter(action -> allConditionsMet(action.conditions(), npc, gameContext))
            .map(action -> new ScoredCandidate(action, calculateScore(action, npc)))
            .max(Comparator.comparingDouble(ScoredCandidate::score));
    }

    private boolean allConditionsMet(List<ConditionSpec> conditions, NpcInstance npc, Object gameContext) {
        if (conditions == null || conditions.isEmpty()) return true;
        return conditions.stream().allMatch(c -> conditionEvaluator.evaluate(c, npc, gameContext));
    }

    private double calculateScore(NpcSpec.ActionSpec action, NpcInstance npc) {
        double base = action.baseScore();
        NpcMood mood = npc.mood();
        double moodMultiplier = 1.0;
        if (mood.loneliness() > 60) moodMultiplier += 0.3;
        if (mood.irritability() > 50) moodMultiplier -= 0.2;
        if (mood.energy() < 30) moodMultiplier -= 0.3;
        if (mood.affection() > 60) moodMultiplier += 0.2;
        double noise = (Math.random() - 0.5) * 0.1;
        return base * Math.max(0.1, moodMultiplier) + noise;
    }

    public record ConditionEvaluator() {
        public boolean evaluate(ConditionSpec condition, NpcInstance npc, Object gameContext) {
            return switch (condition.type()) {
                case "mood" -> evaluateMood(condition, npc);
                case "schedule" -> "available".equals(condition.value());
                case "memory" -> evaluateMemory(condition, npc);
                default -> true;
            };
        }

        private boolean evaluateMood(ConditionSpec c, NpcInstance npc) {
            NpcMood mood = npc.mood();
            int actual = switch (c.target()) {
                case "happiness" -> mood.happiness();
                case "anxiety" -> mood.anxiety();
                case "loneliness" -> mood.loneliness();
                case "irritability" -> mood.irritability();
                case "energy" -> mood.energy();
                case "affection" -> mood.affection();
                default -> 0;
            };
            return switch (c.operator()) {
                case "gte" -> actual >= c.intValue();
                case "lte" -> actual <= c.intValue();
                case "gt" -> actual > c.intValue();
                case "lt" -> actual < c.intValue();
                case "eq" -> actual == c.intValue();
                default -> false;
            };
        }

        private boolean evaluateMemory(ConditionSpec c, NpcInstance npc) {
            return switch (c.target()) {
                case "isBeingIgnored" -> npc.memory().isBeingIgnored();
                case "detectWorkObsession" -> npc.memory().detectWorkObsession();
                default -> false;
            };
        }
    }
}
