package com.sagat9881.lifeoft.domain.npc.model;

/**
 * A time slot in an NPC's daily schedule.
 * Loaded from XML schedule definition.
 * 
 * @param startHour inclusive start hour (0-23)
 * @param endHour exclusive end hour (1-24)
 * @param activityId what the NPC is doing
 * @param locationId where the NPC is
 * @param animationKey animation to play on frontend
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
