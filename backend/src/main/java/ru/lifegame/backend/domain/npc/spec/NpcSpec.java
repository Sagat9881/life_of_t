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

    /**
     * Mood override actions derived from reactions with pattern-type "mood_override".
     *
     * <p>Uses {@link ReactionSpec#triggerAxis()} as the mood axis key so that
     * {@code NpcInstance.checkMoodOverride()} can match it against
     * {@code NpcMood.dominantAxis()} (e.g. "anger", "stress").
     * Falls back to safe defaults when XML parser has not populated new fields.
     */
    // TODO: NpcSpecParser must populate ReactionSpec.triggerAxis, activityId,
    //        locationId, animationKey, durationHours from XML <reaction> elements
    public List<MoodOverrideAction> moodOverrideActions() {
        return reactions.stream()
                .filter(r -> "mood_override".equals(r.patternType()))
                .map(r -> new MoodOverrideAction(
                        r.triggerAxis()  != null && !r.triggerAxis().isBlank()   ? r.triggerAxis()   : "",
                        r.activityId()   != null && !r.activityId().isBlank()    ? r.activityId()    : "",
                        r.locationId()   != null && !r.locationId().isBlank()    ? r.locationId()    : "",
                        r.animationKey() != null && !r.animationKey().isBlank()  ? r.animationKey()  : "",
                        r.durationHours() > 0 ? r.durationHours() : 1
                ))
                .toList();
    }

    public record ScheduleSlot(int start, int end, String activity, String location, String animation) {
        public int startHour()     { return start; }
        public int endHour()       { return end; }
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

    /**
     * Describes an NPC reaction to a game trigger.
     *
     * @param triggerId     unique reaction identifier (e.g. "HUSBAND_COLD")
     * @param patternType   reaction category; "mood_override" activates mood-driven schedule override
     * @param triggerAxis   mood axis whose extreme state triggers this reaction
     *                      (e.g. "anger", "stress"); matched against NpcMood.dominantAxis()
     * @param threshold     axis value threshold at which the reaction fires
     * @param dialogueText  optional dialogue line shown when the reaction triggers
     * @param activityId    activity the NPC switches to when this reaction fires
     * @param locationId    location the NPC moves to when this reaction fires
     * @param animationKey  animation to play when this reaction fires
     * @param durationHours how many hours the overriding activity lasts
     * @param effects       stat/relationship side-effects of the reaction
     */
    public record ReactionSpec(
            String triggerId,
            String patternType,
            String triggerAxis,
            int threshold,
            String dialogueText,
            String activityId,
            String locationId,
            String animationKey,
            int durationHours,
            List<EffectSpec> effects) {}

    public record EffectSpec(String target, String stat, int delta) {}
    public record ConditionSpec(String type, String target, String operator, String value) {}
    public record MoodOverrideAction(String triggerAxis, String activityId, String locationId, String animationKey, int durationHours) {}
}
