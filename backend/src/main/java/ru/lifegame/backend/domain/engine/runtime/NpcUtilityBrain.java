package ru.lifegame.backend.domain.engine.runtime;

import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.engine.ConditionEvaluator;

import java.util.*;
import java.util.stream.Collectors;

public class NpcUtilityBrain {

    private final ConditionEvaluator conditionEvaluator;

    public NpcUtilityBrain(ConditionEvaluator conditionEvaluator) {
        this.conditionEvaluator = conditionEvaluator;
    }

    public Optional<EvaluatedAction> evaluateBest(NpcInstance npc, Map<String, Object> context) {
        if (npc.spec().actions() == null || npc.spec().actions().isEmpty()) {
            return Optional.empty();
        }

        Map<String, Object> enrichedContext = new HashMap<>(context);
        enrichedContext.put("npc_happiness", npc.mood().happiness());
        enrichedContext.put("npc_anxiety", npc.mood().anxiety());
        enrichedContext.put("npc_loneliness", npc.mood().loneliness());
        enrichedContext.put("npc_irritability", npc.mood().irritability());
        enrichedContext.put("npc_energy", npc.mood().energy());
        enrichedContext.put("npc_affection", npc.mood().affection());

        return npc.spec().actions().stream()
                .map(action -> {
                    boolean conditionsMet = action.conditions() == null ||
                            action.conditions().stream()
                                .allMatch(c -> conditionEvaluator.evaluate(c, npc, enrichedContext));
                    if (!conditionsMet) return null;

                    double score = action.baseScore();
                    // Mood-based score modifiers
                    if (action.id().contains("invite") || action.id().contains("call")) {
                        score += npc.mood().loneliness() * 0.01;
                    }
                    if (action.id().contains("concern") || action.id().contains("worry")) {
                        score += npc.mood().anxiety() * 0.008;
                    }
                    if (npc.mood().irritability() > 60) {
                        score *= 0.7;
                    }

                    NpcSpec.ActionSpec actionSpec = action;
                    return new EvaluatedAction(action.id(), score,
                            actionSpec.animation() != null ? actionSpec.animation() : "idle",
                            actionSpec.location() != null ? actionSpec.location() : npc.currentLocation());
                })
                .filter(Objects::nonNull)
                .max(Comparator.comparingDouble(EvaluatedAction::score));
    }

    public record EvaluatedAction(String actionId, double score, String animation, String location) {}
}
