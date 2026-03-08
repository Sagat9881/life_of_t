package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec.*;

import java.util.*;

public class NarrativeEventEngine {

    private final List<EventSpec> eventSpecs;
    private final Map<String, Integer> lastFiredDay = new HashMap<>();

    public NarrativeEventEngine(List<EventSpec> eventSpecs) {
        this.eventSpecs = eventSpecs;
    }

    public record FiredEvent(EventSpec spec, List<EffectSpec> appliedEffects) {}

    public List<FiredEvent> checkEvents(int currentDay, Map<String, Integer> gameState) {
        List<FiredEvent> fired = new ArrayList<>();
        for (EventSpec es : eventSpecs) {
            if (!es.meta().repeatable() && lastFiredDay.containsKey(es.id())) continue;
            if (es.meta().repeatable() && lastFiredDay.containsKey(es.id())) {
                if (currentDay - lastFiredDay.get(es.id()) < es.meta().cooldownDays()) continue;
            }
            if (allConditionsMet(es.triggers(), gameState)) {
                fired.add(new FiredEvent(es, List.of()));
                lastFiredDay.put(es.id(), currentDay);
            }
        }
        fired.sort(Comparator.comparingInt(f -> -f.spec().meta().priority()));
        return fired;
    }

    private boolean allConditionsMet(List<ConditionSpec> conditions, Map<String, Integer> state) {
        for (ConditionSpec c : conditions) {
            Integer val = state.get(c.target());
            if (val == null) return false;
            int threshold = Integer.parseInt(c.value());
            boolean met = switch (c.operator()) {
                case "gte" -> val >= threshold;
                case "lte" -> val <= threshold;
                case "gt" -> val > threshold;
                case "lt" -> val < threshold;
                case "eq" -> val == threshold;
                default -> true;
            };
            if (!met) return false;
        }
        return true;
    }
}
