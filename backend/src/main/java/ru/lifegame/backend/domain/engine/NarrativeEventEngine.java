package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec.ConditionSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec.EffectSpec;

import java.util.*;
import java.util.stream.Collectors;

public class NarrativeEventEngine {

    private final List<EventSpec> eventSpecs;
    private final Set<String> firedOnceEvents = new HashSet<>();

    public NarrativeEventEngine(List<EventSpec> eventSpecs) {
        this.eventSpecs = eventSpecs;
    }

    public List<FiredEvent> evaluate(Map<String, Object> context) {
        List<FiredEvent> result = new ArrayList<>();
        for (EventSpec spec : eventSpecs) {
            if (spec.oneShot() && firedOnceEvents.contains(spec.id())) continue;
            if (allConditionsMet(spec.conditions(), context)) {
                result.add(new FiredEvent(spec, resolveEffects(spec, context)));
                if (spec.oneShot()) firedOnceEvents.add(spec.id());
            }
        }
        return result;
    }

    public record FiredEvent(EventSpec spec, List<EffectSpec> effects) {}

    private List<EffectSpec> resolveEffects(EventSpec spec, Map<String, Object> context) {
        if (spec.effects() == null) return List.of();
        return spec.effects().stream()
                .filter(e -> e.conditions() == null || allConditionsMet(e.conditions(), context))
                .collect(Collectors.toList());
    }

    private boolean allConditionsMet(List<ConditionSpec> conditions, Map<String, Object> context) {
        if (conditions == null || conditions.isEmpty()) return true;
        for (ConditionSpec c : conditions) {
            Object val = context.get(c.target());
            if (val == null) return false;
            if (!evaluateCondition(c, val)) return false;
        }
        return true;
    }

    private boolean evaluateCondition(ConditionSpec c, Object val) {
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
