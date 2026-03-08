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

    public List<FiredEvent> evaluate(Map<String, Object> context, int day) {
        return eventSpecs.stream()
                .filter(e -> !e.once() || !firedOnceEvents.contains(e.id()))
                .filter(e -> matchesConditions(e.conditions(), context, day))
                .map(e -> {
                    if (e.once()) firedOnceEvents.add(e.id());
                    return new FiredEvent(e, e.effects());
                })
                .collect(Collectors.toList());
    }

    public record FiredEvent(EventSpec spec, List<EffectSpec> effects) {}

    private boolean matchesConditions(List<ConditionSpec> conditions, Map<String, Object> ctx, int day) {
        if (conditions == null || conditions.isEmpty()) return true;
        for (ConditionSpec c : conditions) {
            if (!evaluateCondition(c, ctx, day)) return false;
        }
        return true;
    }

    private boolean evaluateCondition(ConditionSpec c, Map<String, Object> ctx, int day) {
        if ("day".equals(c.type())) {
            return evaluateNumeric(day, c.operator(), Integer.parseInt(c.value()));
        }
        Object val = ctx.get(c.target());
        if (val == null) return false;
        if (val instanceof Number num) {
            return evaluateNumeric(num.doubleValue(), c.operator(), Double.parseDouble(c.value()));
        }
        return val.toString().equals(c.value());
    }

    private boolean evaluateNumeric(double actual, String operator, double expected) {
        return switch (operator) {
            case "gte" -> actual >= expected;
            case "lte" -> actual <= expected;
            case "gt" -> actual > expected;
            case "lt" -> actual < expected;
            case "eq" -> actual == expected;
            default -> false;
        };
    }
}
