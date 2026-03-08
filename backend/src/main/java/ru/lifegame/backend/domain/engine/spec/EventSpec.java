package ru.lifegame.backend.domain.engine.spec;

import java.util.List;
import java.util.Map;

public record EventSpec(
    String id,
    EventMeta meta,
    List<ConditionSpec> triggers,
    List<OptionSpec> options
) {
    public record EventMeta(String type, String category, int priority, boolean repeatable, int cooldownDays) {}
    public record OptionSpec(String id, String text, String resultText, EffectSpec effects) {}
    public record EffectSpec(int energy, int stress, int mood, int money, Map<String, Integer> relationships, Map<String, Integer> skills) {}
}
