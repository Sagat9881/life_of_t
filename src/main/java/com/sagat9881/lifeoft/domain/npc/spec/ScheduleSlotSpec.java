package com.sagat9881.lifeoft.domain.npc.spec;

/**
 * A single time slot in an NPC's daily schedule, loaded from XML.
 *
 * @param startHour inclusive start hour (0-23)
 * @param endHour exclusive end hour (1-24)
 * @param activityId what the NPC does (e.g. "breakfast", "work", "sleep")
 * @param locationId where the NPC is (e.g. "kitchen", "away", "bedroom")
 * @param animationKey animation to play on frontend (e.g. "eating", "typing", "sleeping")
 */
public record ScheduleSlotSpec(
        int startHour,
        int endHour,
        String activityId,
        String locationId,
        String animationKey
) {

    public boolean coversHour(int hour) {
        return hour >= startHour && hour < endHour;
    }

    public int durationHours() {
        return endHour - startHour;
    }
}
