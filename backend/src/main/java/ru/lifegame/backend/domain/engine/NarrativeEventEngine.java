package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec.ConditionSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec.EffectSpec;

import java.util.*;
import java.util.stream.Collectors;

public class NarrativeEventEngine {

    private final List<EventSpec> eventSpecs;
    private final Set<String> firedOneTimeEvents = new HashSet<>();

    public NarrativeEventEngine(List<EventSpec> eventSpecs) {
        this.eventSpecs = eventSpecs;
    }

    public List<FiredEvent> evaluate(Map<String, Object> context) {
        return eventSpecs.stream()
                .filter(spec -> !spec.oneTime() || !firedOneTimeEvents.contains(spec.id()))
                .filter(spec -> allConditionsMet(spec.conditions(), context))
                .map(spec -> {
                    if (spec.oneTime()) firedOneTimeEvents.add(spec.id());
                    return new FiredEvent(spec, resolveEffects(spec, context));
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

    private boolean evaluateCondition(ConditionSpec condition, Map<String, Object> context) {
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
