package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec.ConditionSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec.EffectSpec;

import java.util.*;
import java.util.stream.Collectors;

public class NarrativeEventEngine {

    private final List<EventSpec> eventSpecs;
    private final Set<String> firedOnceIds = new HashSet<>();

    public NarrativeEventEngine(List<EventSpec> eventSpecs) {
        this.eventSpecs = eventSpecs;
    }

    public List<FiredEvent> evaluate(Map<String, Object> context) {
        return eventSpecs.stream()
                .filter(e -> !e.once() || !firedOnceIds.contains(e.id()))
                .filter(e -> allConditionsMet(e.conditions(), context))
                .map(e -> {
                    if (e.once()) firedOnceIds.add(e.id());
                    return new FiredEvent(e, resolveEffects(e, context));
                })
                .collect(Collectors.toList());
    }

    public record FiredEvent(EventSpec spec, List<EffectSpec> effects) {}

    private List<EffectSpec> resolveEffects(EventSpec spec, Map<String, Object> context) {
        if (spec.effects() == null) return List.of();
        return spec.effects();
    }

    private boolean allConditionsMet(List<ConditionSpec> conditions, Map<String, Object> context) {
        if (conditions == null || conditions.isEmpty()) return true;
        return conditions.stream().allMatch(c -> evaluateCondition(c, context));
    }

    private boolean evaluateCondition(ConditionSpec c, Map<String, Object> context) {
        Object val = context.get(c.target());
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
