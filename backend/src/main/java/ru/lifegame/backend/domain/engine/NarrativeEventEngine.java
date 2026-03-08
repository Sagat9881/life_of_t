package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec.EffectSpec;
import ru.lifegame.backend.domain.engine.spec.ConditionSpec;

import java.util.*;

public class NarrativeEventEngine {

    private final List<EventSpec> eventSpecs;
    private final Set<String> firedOneTimeEvents = new HashSet<>();
    private final Map<String, Integer> cooldowns = new HashMap<>();

    public NarrativeEventEngine(List<EventSpec> eventSpecs) {
        this.eventSpecs = eventSpecs;
    }

    public record FiredEvent(EventSpec spec, EffectSpec selectedEffect) {}

    public List<FiredEvent> checkEvents(Map<String, Object> context, int currentDay) {
        List<FiredEvent> fired = new ArrayList<>();
        for (var spec : eventSpecs) {
            if (!spec.meta().repeatable() && firedOneTimeEvents.contains(spec.id())) continue;
            if (cooldowns.getOrDefault(spec.id(), 0) > currentDay) continue;
            if (allConditionsMet(spec.triggers(), context)) {
                fired.add(new FiredEvent(spec, null));
                if (!spec.meta().repeatable()) firedOneTimeEvents.add(spec.id());
                if (spec.meta().cooldownDays() > 0) cooldowns.put(spec.id(), currentDay + spec.meta().cooldownDays());
            }
        }
        fired.sort(Comparator.comparingInt((FiredEvent f) -> f.spec().meta().priority()).reversed());
        return fired;
    }

    private boolean allConditionsMet(List<ConditionSpec> conditions, Map<String, Object> context) {
        if (conditions == null || conditions.isEmpty()) return true;
        return conditions.stream().allMatch(c -> evaluateCondition(c, context));
    }

    private boolean evaluateCondition(ConditionSpec c, Map<String, Object> context) {
        Object val = context.get(c.target());
        if (val == null) return false;
        if (val instanceof Number num) {
            int actual = num.intValue();
            return switch (c.operator()) {
                case "gte" -> actual >= c.intValue();
                case "lte" -> actual <= c.intValue();
                case "gt" -> actual > c.intValue();
                case "lt" -> actual < c.intValue();
                case "eq" -> actual == c.intValue();
                default -> false;
            };
        }
        return val.toString().equals(c.value());
    }
}
