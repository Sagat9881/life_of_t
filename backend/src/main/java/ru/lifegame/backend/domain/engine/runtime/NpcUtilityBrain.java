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

    public Optional<ScoredCandidate> evaluate(NpcInstance npc) {
        return npc.spec().actions().stream()
            .filter(a -> allConditionsMet(a.conditions(), npc))
            .map(a -> new ScoredCandidate(a, calculateScore(a, npc)))
            .max(Comparator.comparingDouble(ScoredCandidate::score));
    }

    private boolean allConditionsMet(List<ConditionSpec> conditions, NpcInstance npc) {
        if (conditions == null || conditions.isEmpty()) return true;
        return conditions.stream().allMatch(c -> conditionEvaluator.evaluate(c, npc));
    }

    private double calculateScore(NpcSpec.ActionSpec action, NpcInstance npc) {
        double base = action.baseScore();
        NpcMood m = npc.mood();
        double moodMultiplier = 1.0;
        if (m.loneliness() > 60) moodMultiplier += 0.3;
        if (m.irritability() > 50) moodMultiplier -= 0.2;
        if (m.energy() < 30) moodMultiplier -= 0.3;
        if (m.affection() > 60) moodMultiplier += 0.2;
        return base * Math.max(0.1, moodMultiplier);
    }

    public record ConditionEvaluator() {
        public boolean evaluate(ConditionSpec spec, NpcInstance npc) {
            return switch (spec.type()) {
                case "mood" -> evaluateMood(spec, npc.mood());
                case "schedule" -> true;
                case "memory" -> evaluateMemory(spec, npc);
                default -> true;
            };
        }
        private boolean evaluateMood(ConditionSpec spec, NpcMood mood) {
            int actual = switch (spec.target()) {
                case "happiness" -> mood.happiness();
                case "anxiety" -> mood.anxiety();
                case "loneliness" -> mood.loneliness();
                case "irritability" -> mood.irritability();
                case "energy" -> mood.energy();
                case "affection" -> mood.affection();
                default -> 0;
            };
            return switch (spec.operator()) {
                case "gte" -> actual >= spec.intValue();
                case "lte" -> actual <= spec.intValue();
                case "gt" -> actual > spec.intValue();
                case "lt" -> actual < spec.intValue();
                case "eq" -> actual == spec.intValue();
                default -> false;
            };
        }
        private boolean evaluateMemory(ConditionSpec spec, NpcInstance npc) {
            return switch (spec.target()) {
                case "isBeingIgnored" -> npc.memory().isBeingIgnored();
                case "detectWorkObsession" -> npc.memory().detectWorkObsession();
                default -> false;
            };
        }
    }
}
