package ru.lifegame.backend.domain.npc.spec;

import ru.lifegame.backend.domain.narrative.spec.NarrativeSpec;

import java.util.List;
import java.util.Map;

/**
 * Domain model for an NPC loaded from narrative/npc-behavior/*.xml.
 *
 * <p>Implements {@link NarrativeSpec} so that {@code SpecLoader<NpcSpec>}
 * can operate generically without switch/if on spec types.
 *
 * <p>Ref: java-developer-skill.md §3.1, §7 (Domain — no outbound deps to infra),
 *         TASK-BE-018.
 */
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
) implements NarrativeSpec {

    @Override
    public String getId() { return id; }

    @Override
    public String getBlockId() { return "npc-behavior"; }

    public boolean isNamed()  { return "named".equals(type); }
    public boolean isFiller() { return "filler".equals(type); }

    /** Alias for shortTermSize for backward compatibility. */
    public int memoryShortTermSize() { return shortTermSize; }

    /** Alias for schedule for backward compatibility. */
    public List<ScheduleSlot> scheduleSlots() { return schedule; }

    /**
     * Mood override actions derived from reactions with pattern-type "mood_override".
     * Falls back to safe defaults when XML parser has not populated new fields.
     */
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
        public int startHour()       { return start; }
        public int endHour()         { return end; }
        public String activityId()   { return activity; }
        public String locationId()   { return location; }
        public String animationKey() { return animation; }
    }

    public record ActionSpec(
            String actionId,
            double baseScore,
            String eventType,
            String animationKey,
            String locationId,
            List<ConditionSpec> conditions,
            List<OptionSpec> options) {}

    public record OptionSpec(String optionId, String text, String result, int energy, int stress, int mood, int money, String relationshipTarget, int relationshipDelta) {}

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
