package ru.lifegame.backend.domain.npc.spec;

import java.util.List;

public record EventSpec(
    String id,
    EventMeta meta,
    List<ConditionSpec> triggers,
    List<OptionSpec> options
) {
    public record EventMeta(String title, String description, String type, int priority, boolean repeatable, int cooldownDays) {}
    public record ConditionSpec(String type, String target, String operator, String value) {}
    public record OptionSpec(String optionId, String text, String result, List<EffectSpec> effects) {}
    public record EffectSpec(String target, String stat, int delta) {}
}
