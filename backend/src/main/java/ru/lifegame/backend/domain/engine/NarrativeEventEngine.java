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
        List<FiredEvent> results = new ArrayList<>();
        for (EventSpec spec : eventSpecs) {
            if (spec.once() && firedOnceIds.contains(spec.id())) continue;
            if (allConditionsMet(spec.conditions(), context)) {
                results.add(new FiredEvent(spec, spec.effects()));
                if (spec.once()) firedOnceIds.add(spec.id());
            }
        }
        return results;
    }

    public record FiredEvent(EventSpec spec, List<EffectSpec> effects) {}

    private boolean allConditionsMet(List<ConditionSpec> conditions, Map<String, Object> context) {
        if (conditions == null || conditions.isEmpty()) return true;
        for (ConditionSpec cond : conditions) {
            if (!evaluateCondition(cond, context)) return false;
        }
        return true;
    }

    private boolean evaluateCondition(ConditionSpec cond, Map<String, Object> context) {
        Object val = context.get(cond.target());
        if (val == null) return false;
        if (val instanceof Number num) {
            double v = num.doubleValue();
            return switch (cond.operator()) {
                case "gte" -> v >= cond.value();
                case "lte" -> v <= cond.value();
                case "gt" -> v > cond.value();
                case "lt" -> v < cond.value();
                case "eq" -> v == cond.value();
                default -> false;
            };
        }
        return false;
    }
}
