package com.life_of_t.domain.npc.spec;

import java.util.List;
import java.util.Map;

/**
 * Immutable specification of an NPC loaded from XML.
 * The engine works exclusively with these abstractions —
 * it never hardcodes NPC names, actions, or locations.
 */
public record NpcSpec(
        String id,
        String type,        // "named" or "filler"
        String category,    // "human", "animal", etc.
        String displayName,
        Map<String, Integer> personality,
        Map<String, Integer> moodInitial,
        boolean memoryEnabled,
        int memoryShortTermSize,
        List<ScheduleSlotSpec> scheduleSlots,
        List<ScoredAction> actions,
        List<MoodOverrideAction> moodOverrideActions,
        List<String> questLines,
        String interactionActionId
) {

    public boolean isNamed() {
        return "named".equals(type);
    }

    public boolean isFiller() {
        return "filler".equals(type);
    }

    public int personalityTrait(String trait) {
        return personality.getOrDefault(trait, 50);
    }

    /**
     * Schedule slot from XML.
     */
    public record ScheduleSlotSpec(
            int startHour,
            int endHour,
            String activityId,
            String locationId,
            String animationKey
    ) {}

    /**
     * Mood-driven schedule override from XML.
     * When NPC mood axis exceeds extreme threshold,
     * this activity replaces the normal schedule.
     */
    public record MoodOverrideAction(
            String triggerAxis,
            String activityId,
            String locationId,
            String animationKey,
            int durationHours
    ) {}
}
