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
        this.eventSpecs = eventSpecs != null ? eventSpecs : List.of();
    }

    public record FiredEvent(String eventId, String type, String title, String description,
                             List<EventOption> options) {}

    public record EventOption(String optionId, String text, String resultText,
                              EventSpec spec, EffectSpec effect) {}

    public List<FiredEvent> checkEvents(Map<String, Object> context) {
        List<FiredEvent> result = new ArrayList<>();
        for (EventSpec spec : eventSpecs) {
            if (firedOneTimeEvents.contains(spec.id())) continue;
            if (allConditionsMet(spec.conditions(), context)) {
                result.add(toFiredEvent(spec));
                if ("one_time".equals(spec.type())) {
                    firedOneTimeEvents.add(spec.id());
                }
            }
        }
        return result;
    }

    private FiredEvent toFiredEvent(EventSpec spec) {
        List<EventOption> options = spec.options().stream()
                .map(o -> new EventOption(o.id(), o.text(), o.resultText(), spec, o.effect()))
                .collect(Collectors.toList());
        return new FiredEvent(spec.id(), spec.type(), spec.title(), spec.description(), options);
    }

    private boolean allConditionsMet(List<ConditionSpec> conditions, Map<String, Object> ctx) {
        if (conditions == null || conditions.isEmpty()) return true;
        return conditions.stream().allMatch(c -> evaluateCondition(c, ctx));
    }

    private boolean evaluateCondition(ConditionSpec cond, Map<String, Object> ctx) {
        Object val = ctx.get(cond.target());
        if (val == null) return false;
        if (val instanceof Number num) {
            double v = num.doubleValue();
            double threshold = Double.parseDouble(cond.value());
            return switch (cond.operator()) {
                case "gte" -> v >= threshold;
                case "lte" -> v <= threshold;
                case "gt" -> v > threshold;
                case "lt" -> v < threshold;
                case "eq" -> v == threshold;
                default -> false;
            };
        }
        return String.valueOf(val).equals(cond.value());
    }
}
