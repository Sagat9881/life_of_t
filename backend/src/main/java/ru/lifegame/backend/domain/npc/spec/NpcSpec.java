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

    /** Alias for shortTermSize for backward compatibility. */
    public int memoryShortTermSize() { return shortTermSize; }

    /** Alias for schedule for backward compatibility. */
    public List<ScheduleSlot> scheduleSlots() { return schedule; }

    /** Mood override actions derived from reactions with pattern-type "mood_override". */
    public List<MoodOverrideAction> moodOverrideActions() {
        return reactions.stream()
                .filter(r -> "mood_override".equals(r.patternType()))
                .map(r -> new MoodOverrideAction(r.triggerId(), r.dialogueText(), "", "", 1))
                .toList();
    }

    public record ScheduleSlot(int start, int end, String activity, String location, String animation) {
        public int startHour() { return start; }
        public int endHour() { return end; }
        public String activityId() { return activity; }
        public String locationId() { return location; }
        public String animationKey() { return animation; }
    }

    /**
     * Defines a scored action the NPC utility brain can select.
     *
     * @param animationKey animation to play when this action is active;
     *                     populated by NpcSpecParser from XML attribute animationKey.
     *                     Falls back to actionId + "_anim" if absent in XML.
     * @param locationId   location where the action takes place;
     *                     populated by NpcSpecParser from XML attribute locationId.
     *                     Falls back to "default" if absent in XML.
     */
    // TODO: NpcSpecParser must populate ActionSpec.animationKey and locationId from XML
    public record ActionSpec(
            String actionId,
            double baseScore,
            String eventType,
            String animationKey,
            String locationId,
            List<ConditionSpec> conditions,
            List<OptionSpec> options) {}

    public record OptionSpec(String optionId, String text, String result, int energy, int stress, int mood, int money, String relationshipTarget, int relationshipDelta) {}
    public record ReactionSpec(String triggerId, String patternType, int threshold, String dialogueText, List<EffectSpec> effects) {}
    public record EffectSpec(String target, String stat, int delta) {}
    public record ConditionSpec(String type, String target, String operator, String value) {}
    public record MoodOverrideAction(String triggerAxis, String activityId, String locationId, String animationKey, int durationHours) {}
}
