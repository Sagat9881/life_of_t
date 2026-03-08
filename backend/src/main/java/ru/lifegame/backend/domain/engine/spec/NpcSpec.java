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
    List<NpcSpec.ScheduleSlot> scheduleSlots,
    List<NpcSpec.ActionSpec> actions,
    List<NpcSpec.ReactionSpec> reactions,
    List<String> questLines
) {
    public record ScheduleSlot(int start, int end, String activity, String location, String animation) {}
    public record ActionSpec(String id, double baseScore, String eventType,
                             List<ConditionSpec> conditions, List<ActionOption> options) {}
    public record ActionOption(String id, String text, String result,
                               int energy, int stress, int mood, int money,
                               String relationshipTarget, int relationshipDelta) {}
    public record ReactionSpec(String triggerId, List<ConditionSpec> conditions,
                               String dialogueText, Map<String, Integer> statEffects) {}
}
