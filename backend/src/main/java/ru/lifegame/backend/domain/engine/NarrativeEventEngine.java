package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec.ConditionSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec.EffectSpec;

import java.util.*;
import java.util.stream.Collectors;

public class NarrativeEventEngine {

    private final List<EventSpec> allEvents;
    private final Set<String> firedEventIds = new HashSet<>();

    public NarrativeEventEngine(List<EventSpec> allEvents) {
        this.allEvents = allEvents;
    }

    public record FiredEvent(
            String eventId,
            String title,
            String description,
            List<EventSpec.OptionSpec> options
    ) {}

    public List<FiredEvent> checkEvents(Map<String, Object> context) {
        return allEvents.stream()
                .filter(e -> !firedEventIds.contains(e.id()))
                .filter(e -> !e.oneShot() || !firedEventIds.contains(e.id()))
                .filter(e -> allConditionsMet(e.conditions(), context))
                .map(e -> {
                    firedEventIds.add(e.id());
                    return new FiredEvent(e.id(), e.title(), e.description(), e.options());
                })
                .collect(Collectors.toList());
    }

    public List<EffectSpec> resolveOption(String eventId, String optionId) {
        return allEvents.stream()
                .filter(e -> e.id().equals(eventId))
                .flatMap(e -> e.options().stream())
                .filter(o -> o.id().equals(optionId))
                .flatMap(o -> o.effects().stream())
                .collect(Collectors.toList());
    }

    private boolean allConditionsMet(List<ConditionSpec> conditions, Map<String, Object> context) {
        return conditions.stream().allMatch(c -> evaluateCondition(c, context));
    }

    private boolean evaluateCondition(ConditionSpec c, Map<String, Object> context) {
        Object val = context.get(c.target());
        if (val == null) return false;
        if (val instanceof Number num) {
            double actual = num.doubleValue();
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
        return val.toString().equals(c.value());
    }
}
