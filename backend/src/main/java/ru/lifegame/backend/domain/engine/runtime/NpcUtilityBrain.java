package ru.lifegame.backend.domain.engine.runtime;

import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.engine.NpcMood;

import java.util.*;

public class NpcUtilityBrain {

    public record ScoredResult(String actionId, double score, String animation, String location) {}

    public Optional<ScoredResult> evaluate(NpcInstance npc, Map<String, Object> context) {
        NpcSpec spec = npc.spec();
        NpcMood mood = npc.mood();
        List<ScoredResult> candidates = new ArrayList<>();

        for (NpcSpec.ActionSpec action : spec.actions()) {
            double score = action.baseScore();
            // Mood-based scoring adjustments
            score += mood.loneliness() * 0.01 * (action.id().contains("invite") || action.id().contains("call") ? 1.5 : 0.2);
            score += mood.irritability() * 0.01 * (action.id().contains("criticism") ? 1.2 : -0.3);
            score += mood.happiness() * 0.01 * (action.id().contains("movie") || action.id().contains("play") ? 1.0 : 0.1);
            score -= mood.energy() < 30 ? 0.3 : 0;

            if (allConditionsMet(action.conditions(), npc, context)) {
                candidates.add(new ScoredResult(action.id(), score, action.animation(), action.location()));
            }
        }

        return candidates.stream().max(Comparator.comparingDouble(ScoredResult::score));
    }

    private boolean allConditionsMet(List<NpcSpec.ConditionSpec> conditions, NpcInstance npc, Map<String, Object> context) {
        if (conditions == null || conditions.isEmpty()) return true;
        return conditions.stream().allMatch(c -> evaluateCondition(c, npc, context));
    }

    private boolean evaluateCondition(NpcSpec.ConditionSpec c, NpcInstance npc, Map<String, Object> context) {
        double actual = switch (c.type()) {
            case "mood" -> switch (c.target()) {
                case "happiness" -> npc.mood().happiness();
                case "anxiety" -> npc.mood().anxiety();
                case "loneliness" -> npc.mood().loneliness();
                case "irritability" -> npc.mood().irritability();
                case "energy" -> npc.mood().energy();
                case "affection" -> npc.mood().affection();
                default -> 0;
            };
            case "context" -> {
                Object val = context.get(c.target());
                yield val instanceof Number n ? n.doubleValue() : 0;
            }
            default -> 0;
        };

        double expected = Double.parseDouble(c.value());
        return switch (c.operator()) {
            case "gte" -> actual >= expected;
            case "lte" -> actual <= expected;
            case "gt" -> actual > expected;
            case "lt" -> actual < expected;
            case "eq" -> actual == expected;
            default -> false;
        };
    }
}
