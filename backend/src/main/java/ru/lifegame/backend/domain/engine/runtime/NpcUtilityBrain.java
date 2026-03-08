package ru.lifegame.backend.domain.engine.runtime;

import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.engine.spec.ConditionSpec;
import ru.lifegame.backend.domain.npc.engine.NpcMood;

import java.util.ArrayList;
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
        List<ScoredCandidate> candidates = new ArrayList<>();

        for (NpcSpec.ActionSpec action : npc.spec().actions()) {
            boolean allMet = true;
            for (ConditionSpec cond : action.conditions()) {
                if (!conditionEvaluator.evaluate(cond, npc, gameContext)) {
                    allMet = false;
                    break;
                }
            }
            if (allMet) {
                double score = computeScore(action, npc);
                candidates.add(new ScoredCandidate(action, score));
            }
        }

        return candidates.stream()
                .max(Comparator.comparingDouble(ScoredCandidate::score));
    }

    private double computeScore(NpcSpec.ActionSpec action, NpcInstance npc) {
        double base = action.baseScore();
        NpcMood mood = npc.mood();
        if (mood.loneliness() > 60) base += 0.2;
        if (mood.irritability() > 50) base -= 0.15;
        if (mood.happiness() > 70) base += 0.1;
        if (mood.energy() < 30) base -= 0.2;
        return Math.max(0, base);
    }
}
