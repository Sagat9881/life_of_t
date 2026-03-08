package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.ConditionSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec.EffectSpec;

import java.util.*;
import java.util.stream.Collectors;

public class NarrativeEventEngine {

    private final List<EventSpec> eventSpecs;
    private final Set<String> firedOnceIds = new HashSet<>();

    public NarrativeEventEngine(List<EventSpec> eventSpecs) {
        this.eventSpecs = eventSpecs;
    }

    public record FiredEvent(
            String eventId,
            String type,
            EventSpec spec,
            EffectSpec chosenEffect
    ) {}

    public List<EventSpec> checkTriggers(Map<String, Object> context) {
        List<EventSpec> triggered = new ArrayList<>();
        for (EventSpec spec : eventSpecs) {
            if (spec.oneShot() && firedOnceIds.contains(spec.id())) continue;
            boolean allMet = spec.conditions().stream()
                    .allMatch(c -> evaluateCondition(c, context));
            if (allMet) {
                triggered.add(spec);
                if (spec.oneShot()) firedOnceIds.add(spec.id());
            }
        }
        return triggered;
    }

    private boolean evaluateCondition(ConditionSpec condition, Map<String, Object> context) {
        Object value = context.get(condition.target());
        if (value == null) return false;
        if (value instanceof Number num) {
            double v = num.doubleValue();
            double threshold = Double.parseDouble(condition.value());
            return switch (condition.operator()) {
                case "gte" -> v >= threshold;
                case "lte" -> v <= threshold;
                case "gt" -> v > threshold;
                case "lt" -> v < threshold;
                case "eq" -> v == threshold;
                default -> false;
            };
        }
        return String.valueOf(value).equals(condition.value());
    }
}
