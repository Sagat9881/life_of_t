package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.runtime.NpcInstance;

import java.util.*;
import java.util.stream.Collectors;

public class CrossNpcTriggerEngine {

    private final List<CrossNpcTrigger> triggers;

    public CrossNpcTriggerEngine(List<CrossNpcTrigger> triggers) {
        this.triggers = triggers != null ? triggers : List.of();
    }

    public record CrossNpcTrigger(
            String id,
            String sourceNpcId,
            String targetNpcId,
            String conditionType,
            String operator,
            String value,
            String effectType,
            String effectValue
    ) {}

    public record FiredTrigger(String triggerId, String sourceNpcId, String targetNpcId, String effectType, String effectValue) {}

    public List<FiredTrigger> evaluate(NpcRegistry registry, Map<String, Object> context) {
        List<FiredTrigger> fired = new ArrayList<>();
        for (CrossNpcTrigger trigger : triggers) {
            if (checkTriggerCondition(trigger, registry, context)) {
                fired.add(new FiredTrigger(
                        trigger.id(), trigger.sourceNpcId(), trigger.targetNpcId(),
                        trigger.effectType(), trigger.effectValue()));
            }
        }
        return fired;
    }

    private boolean checkTriggerCondition(CrossNpcTrigger trigger, NpcRegistry registry, Map<String, Object> context) {
        String key = trigger.conditionType() + "." + trigger.sourceNpcId();
        Object val = context.get(key);
        if (val == null) return false;
        if (val instanceof Number num) {
            double actual = num.doubleValue();
            double expected = Double.parseDouble(trigger.value());
            return switch (trigger.operator()) {
                case "gte" -> actual >= expected;
                case "lte" -> actual <= expected;
                case "gt" -> actual > expected;
                case "lt" -> actual < expected;
                default -> false;
            };
        }
        return false;
    }
}
