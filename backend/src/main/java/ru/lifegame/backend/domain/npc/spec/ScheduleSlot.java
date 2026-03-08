package ru.lifegame.backend.domain.npc.spec;

/**
 * A single schedule slot for an NPC's daily routine.
 * Loaded from XML schedule definition.
 */
public record ScheduleSlot(
    int start,
    int end,
    String activity,
    String location,
    String animation
) {
    /**
     * Alias accessors for backward compatibility with different naming conventions.
     */
    public int startHour() { return start; }
    public int endHour() { return end; }
    public String activityId() { return activity; }
    public String locationId() { return location; }
    public String animationKey() { return animation; }
}
