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

    public record FiredEvent(
        String eventId,
        String title,
        String description,
        List<OptionView> options
    ) {}

    public record OptionView(
        String optionId,
        String text,
        EventSpec spec,
        EffectSpec effect
    ) {}

    public List<FiredEvent> evaluate(Map<String, Object> context) {
        List<FiredEvent> result = new ArrayList<>();
        for (EventSpec spec : eventSpecs) {
            if (spec.fireOnce() && firedOnceIds.contains(spec.id())) continue;
            if (allConditionsMet(spec.conditions(), context)) {
                result.add(toFiredEvent(spec));
                if (spec.fireOnce()) firedOnceIds.add(spec.id());
            }
        }
        return result;
    }

    private FiredEvent toFiredEvent(EventSpec spec) {
        List<OptionView> options = spec.options().stream()
            .map(o -> new OptionView(o.id(), o.text(), spec, o.effect()))
            .collect(Collectors.toList());
        return new FiredEvent(spec.id(), spec.title(), spec.description(), options);
    }

    private boolean allConditionsMet(List<ConditionSpec> conditions, Map<String, Object> context) {
        if (conditions == null || conditions.isEmpty()) return true;
        return conditions.stream().allMatch(c -> evaluateCondition(c, context));
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
