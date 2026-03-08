package ru.lifegame.backend.domain.npc.spec;

import java.util.List;
import java.util.Map;

public record NpcSpec(
    String id,
    String type,
    String category,
    String displayName,
    Map<String, Integer> personalityTraits,
    Map<String, Integer> moodInitial,
    boolean memoryEnabled,
    int shortTermSize,
    List<ScheduleSlot> schedule,
    List<ActionSpec> actions,
    List<ReactionSpec> reactions,
    List<String> questLines
) {
    public boolean isNamed() { return "named".equals(type); }
    public boolean isFiller() { return "filler".equals(type); }

    public record ScheduleSlot(int start, int end, String activity, String location, String animation) {}
    public record ActionSpec(String actionId, double baseScore, String eventType, List<ConditionSpec> conditions, List<OptionSpec> options) {}
    public record OptionSpec(String optionId, String text, String result, int energy, int stress, int mood, int money, String relationshipTarget, int relationshipDelta) {}
    public record ReactionSpec(String triggerId, String patternType, int threshold, String dialogueText, List<EffectSpec> effects) {}
    public record EffectSpec(String target, String stat, int delta) {}
    public record ConditionSpec(String type, String target, String operator, String value) {}
}
