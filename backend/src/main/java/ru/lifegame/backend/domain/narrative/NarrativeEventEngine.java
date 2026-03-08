package ru.lifegame.backend.domain.narrative;


import ru.lifegame.backend.domain.narrative.spec.EventSpec;

import java.util.*;
import java.util.stream.Collectors;

public class NarrativeEventEngine {

    private final List<EventSpec> eventSpecs;

    public NarrativeEventEngine(List<EventSpec> eventSpecs) {
        this.eventSpecs = eventSpecs != null ? eventSpecs : List.of();
    }

    public List<FiredEvent> evaluate(Map<String, Object> context) {
        return eventSpecs.stream()
                .filter(spec -> allConditionsMet(spec.triggers(), context))
                .map(spec -> new FiredEvent(spec, List.of()))
                .collect(Collectors.toList());
    }

    public record FiredEvent(
            EventSpec spec,
            List<EventSpec.EffectSpec> appliedEffects
    ) {}

    private boolean allConditionsMet(List<EventSpec.ConditionSpec> conditions, Map<String, Object> context) {
        if (conditions == null || conditions.isEmpty()) return true;
        return conditions.stream().allMatch(c -> evaluateCondition(c, context));
    }

    private boolean evaluateCondition(EventSpec.ConditionSpec condition, Map<String, Object> context) {
        Object value = context.get(condition.target());
        if (value == null) return false;
        if (value instanceof Number num) {
            double actual = num.doubleValue();
            double expected = Double.parseDouble(condition.value());
            return switch (condition.operator()) {
                case "gte" -> actual >= expected;
                case "lte" -> actual <= expected;
                case "gt" -> actual > expected;
                case "lt" -> actual < expected;
                case "eq" -> actual == expected;
                default -> false;
            };
        }
        return value.toString().equals(condition.value());
    }
}
