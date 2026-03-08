package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.ConditionSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec.OptionSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec.EffectSpec;

import java.util.*;
import java.util.stream.Collectors;

public class NarrativeEventEngine {

    private final List<EventSpec> allEvents;
    private final Set<String> firedOneTimeEvents = new HashSet<>();

    public NarrativeEventEngine(List<EventSpec> allEvents) {
        this.allEvents = allEvents;
    }

    public record FiredEvent(
            String eventId,
            String displayText,
            List<OptionSpec> options,
            EventSpec spec,
            EffectSpec autoEffect
    ) {}

    public List<FiredEvent> checkEvents(Map<String, Object> gameContext) {
        List<FiredEvent> result = new ArrayList<>();
        for (EventSpec event : allEvents) {
            if (event.oneTime() && firedOneTimeEvents.contains(event.id())) continue;
            if (allConditionsMet(event.conditions(), gameContext)) {
                result.add(new FiredEvent(
                        event.id(), event.displayText(), event.options(), event, event.autoEffect()));
                if (event.oneTime()) firedOneTimeEvents.add(event.id());
            }
        }
        return result;
    }

    private boolean allConditionsMet(List<ConditionSpec> conditions, Map<String, Object> ctx) {
        if (conditions == null || conditions.isEmpty()) return true;
        return conditions.stream().allMatch(c -> evaluateCondition(c, ctx));
    }

    private boolean evaluateCondition(ConditionSpec c, Map<String, Object> ctx) {
        Object val = ctx.get(c.target());
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
        return String.valueOf(val).equals(c.value());
    }
}
