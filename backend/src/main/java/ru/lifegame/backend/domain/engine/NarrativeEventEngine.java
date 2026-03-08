package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.ConditionSpec;
import ru.lifegame.backend.domain.engine.spec.EffectSpec;

import java.util.*;

public class NarrativeEventEngine {

    private final List<EventSpec> eventSpecs;

    public NarrativeEventEngine(List<EventSpec> eventSpecs) {
        this.eventSpecs = eventSpecs;
    }

    public List<FiredEvent> checkEvents(Map<String, Object> gameState) {
        List<FiredEvent> fired = new ArrayList<>();
        for (EventSpec spec : eventSpecs) {
            if (allConditionsMet(spec.conditions(), gameState)) {
                fired.add(new FiredEvent(spec, spec.effects()));
            }
        }
        return fired;
    }

    public record FiredEvent(EventSpec spec, List<EffectSpec> effects) {}

    private boolean allConditionsMet(List<ConditionSpec> conditions, Map<String, Object> state) {
        if (conditions == null || conditions.isEmpty()) return false;
        for (ConditionSpec c : conditions) {
            if (!evaluateCondition(c, state)) return false;
        }
        return true;
    }

    private boolean evaluateCondition(ConditionSpec c, Map<String, Object> state) {
        Object val = state.get(c.target());
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
        return String.valueOf(val).equals(c.value());
    }
}
