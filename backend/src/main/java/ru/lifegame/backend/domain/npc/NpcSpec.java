package ru.lifegame.backend.domain.npc;

import java.util.List;
import java.util.Map;

/**
 * Immutable specification of an NPC loaded from XML.
 * Backend never knows concrete NPC names — only this abstract spec.
 *
 * type: "named" (full brain: 6-axis mood, memory, utility AI)
 *       "filler" (light brain: 2-axis mood, fixed schedule, no memory)
 * category: "human", "animal", etc. — for UI rendering hints
 */
public record NpcSpec(
    String id,
    String type,
    String category,
    String displayName,
    Map<String, Integer> personalityTraits,
    MoodInitial moodInitial,
    boolean memoryEnabled,
    int shortTermMemorySize,
    List<ScheduleSlot> scheduleSlots,
    List<ScoredAction> actions,
    List<String> questLines
) {
    public boolean isNamed() {
        return "named".equalsIgnoreCase(type);
    }

    public boolean isFiller() {
        return "filler".equalsIgnoreCase(type);
    }

    public int personalityTrait(String name) {
        return personalityTraits.getOrDefault(name, 50);
    }

    /**
     * Initial mood values from XML. Missing axes default to 50.
     */
    public record MoodInitial(
        int happiness,
        int anxiety,
        int loneliness,
        int irritability,
        int energy,
        int affection
    ) {
        public static MoodInitial defaults() {
            return new MoodInitial(50, 20, 30, 10, 70, 50);
        }
    }

    /**
     * A time slot in NPC's daily schedule.
     */
    public record ScheduleSlot(
        int startHour,
        int endHour,
        String activityId,
        String locationId,
        String animationKey
    ) {
        public boolean containsHour(int hour) {
            return hour >= startHour && hour < endHour;
        }
    }
}
