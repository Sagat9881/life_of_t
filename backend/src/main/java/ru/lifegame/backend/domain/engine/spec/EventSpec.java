package ru.lifegame.backend.domain.engine.spec;

import java.util.List;
import java.util.Map;

public record EventSpec(
    String id,
    EventMeta meta,
    List<ConditionSpec> triggers,
    List<EventOption> options
) {
    public record EventMeta(String type, String title, String description, int priority, boolean repeatable) {}
    public record EventOption(String id, String text, String resultText, List<EffectSpec> effects) {}
    public record EffectSpec(String type, String target, int value) {}
}
