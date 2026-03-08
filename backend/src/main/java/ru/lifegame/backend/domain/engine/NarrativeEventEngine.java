package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec.ConditionSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec.EffectSpec;

import java.util.*;
import java.util.stream.Collectors;

public class NarrativeEventEngine {

    private final List<EventSpec> eventSpecs;
    private final Set<String> firedEventIds = new HashSet<>();

    public NarrativeEventEngine(List<EventSpec> eventSpecs) {
        this.eventSpecs = eventSpecs != null ? eventSpecs : List.of();
    }

    public List<FiredEvent> evaluate(Map<String, Object> context) {
        List<FiredEvent> result = new ArrayList<>();
        for (EventSpec spec : eventSpecs) {
            if (firedEventIds.contains(spec.id()) && !spec.repeatable()) continue;
            if (allConditionsMet(spec.conditions(), context)) {
                firedEventIds.add(spec.id());
                result.add(new FiredEvent(spec, spec.effects()));
            }
        }
        return result;
    }

    public record FiredEvent(EventSpec spec, List<EffectSpec> effects) {}

    private boolean allConditionsMet(List<ConditionSpec> conditions, Map<String, Object> ctx) {
        if (conditions == null || conditions.isEmpty()) return true;
        return conditions.stream().allMatch(c -> evaluateCondition(c, ctx));
    }

    private boolean evaluateCondition(ConditionSpec c, Map<String, Object> ctx) {
        Object val = ctx.get(c.target());
        if (val == null) return false;
        if (val instanceof Number num) {
            double v = num.doubleValue();
            double threshold = Double.parseDouble(c.value());
            return switch (c.operator()) {
                case "gte" -> v >= threshold;
                case "lte" -> v <= threshold;
                case "gt" -> v > threshold;
                case "lt" -> v < threshold;
                case "eq" -> v == threshold;
                default -> false;
            };
        }
        return val.toString().equals(c.value());
    }
}
