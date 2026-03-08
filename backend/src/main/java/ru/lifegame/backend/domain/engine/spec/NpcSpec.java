package ru.lifegame.backend.domain.engine.spec;

import java.util.List;
import java.util.Map;

public record NpcSpec(
    String id,
    String type,
    String category,
    String displayName,
    Map<String, Integer> personality,
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
    public record ActionSpec(String id, double baseScore, String eventType, List<ConditionSpec> conditions, List<OptionSpec> options) {}
    public record OptionSpec(String id, String text, String result, int energy, int stress, int mood, int money, String relationshipTarget, int relationshipDelta) {}
    public record ReactionSpec(String id, String trigger, String type, List<ConditionSpec> conditions, String dialogueText, Map<String, Integer> effects) {}
}
