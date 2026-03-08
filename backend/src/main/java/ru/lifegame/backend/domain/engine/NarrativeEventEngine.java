package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec.ConditionSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec.EffectSpec;

import java.util.*;
import java.util.stream.Collectors;

public class NarrativeEventEngine {

    private final List<EventSpec> allEvents;
    private final Set<String> firedEventIds = new HashSet<>();

    public NarrativeEventEngine(List<EventSpec> events) {
        this.allEvents = new ArrayList<>(events);
    }

    public record FiredEvent(String eventId, String text, List<OptionResult> options) {}
    public record OptionResult(String optionId, String text, Map<String, Integer> statChanges, String npcTarget, int relationshipDelta) {}

    public Optional<FiredEvent> checkForEvent(Map<String, Object> gameContext) {
        for (EventSpec event : allEvents) {
            if (firedEventIds.contains(event.id())) continue;
            if (allConditionsMet(event.conditions(), gameContext)) {
                firedEventIds.add(event.id());
                List<OptionResult> options = event.options().stream()
                    .map(o -> new OptionResult(o.id(), o.text(), o.statChanges(), o.npcTarget(), o.relationshipDelta()))
                    .collect(Collectors.toList());
                return Optional.of(new FiredEvent(event.id(), event.text(), options));
            }
        }
        return Optional.empty();
    }

    private boolean allConditionsMet(List<ConditionSpec> conditions, Map<String, Object> ctx) {
        if (conditions == null || conditions.isEmpty()) return true;
        for (ConditionSpec c : conditions) {
            if (!evaluateCondition(c, ctx)) return false;
        }
        return true;
    }

    private boolean evaluateCondition(ConditionSpec c, Map<String, Object> ctx) {
        Object val = ctx.get(c.target());
        if (val == null) return false;
        if (val instanceof Number num) {
            double v = num.doubleValue();
            return switch (c.operator()) {
                case "gte" -> v >= c.value();
                case "lte" -> v <= c.value();
                case "gt" -> v > c.value();
                case "lt" -> v < c.value();
                case "eq" -> v == c.value();
                default -> false;
            };
        }
        return false;
    }
}
