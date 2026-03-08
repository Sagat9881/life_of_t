package ru.lifegame.backend.domain.npc.spec;

import java.util.List;
import java.util.Map;

/**
 * NPC specification loaded from XML.
 * Immutable value object — the canonical source of NPC definition.
 * Both Named and Filler NPCs are described by NpcSpec with different depth.
 */
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
    List<ScoredAction> actions,
    List<String> questLines
) {
    public boolean isNamed() { return "named".equals(type); }
    public boolean isFiller() { return "filler".equals(type); }

    /**
     * Alias for shortTermSize for backward compatibility.
     */
    public int memoryShortTermSize() { return shortTermSize; }

    /**
     * Schedule slots alias.
     */
    public List<ScheduleSlot> scheduleSlots() { return schedule; }

    /**
     * Mood override actions — not yet supported via this spec, returns empty list.
     * Override actions are loaded separately in NpcSpecLoader.
     */
    public List<MoodOverrideAction> moodOverrideActions() { return List.of(); }

    public record MoodOverrideAction(
        String triggerAxis,
        String activityId,
        String locationId,
        String animationKey,
        int durationHours
    ) {}
}
