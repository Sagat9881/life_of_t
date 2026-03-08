package com.sagat9881.lifeoft.domain.npc.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * NPC daily schedule constructed from XML-defined slots.
 * No hardcoded schedules — all data comes from NpcSpec.
 * Each slot defines: start hour, end hour, activity, location, animation.
 */
public class NpcSchedule {

    private final List<ScheduleSlot> slots;

    private NpcSchedule(List<ScheduleSlot> slots) {
        this.slots = List.copyOf(slots);
    }

    public static NpcSchedule fromSlots(List<ScheduleSlot> slots) {
        return new NpcSchedule(slots != null ? slots : List.of());
    }

    /**
     * Get the schedule slot active at the given hour.
     */
    public Optional<ScheduleSlot> getSlotForHour(int hour) {
        return slots.stream()
                .filter(s -> hour >= s.startHour() && hour < s.endHour())
                .findFirst();
    }

    /**
     * Is the NPC available for interaction at this hour?
     * NPCs at 'away' or 'sleep' locations are not available.
     */
    public boolean isAvailable(int hour) {
        return getSlotForHour(hour)
                .map(s -> !"away".equals(s.location()) && !"sleeping".equals(s.animation()))
                .orElse(true);
    }

    /**
     * Is the NPC at home at this hour?
     */
    public boolean isAtHome(int hour) {
        return getSlotForHour(hour)
                .map(s -> !"away".equals(s.location()))
                .orElse(true);
    }

    public List<ScheduleSlot> allSlots() {
        return Collections.unmodifiableList(slots);
    }

    /**
     * A single time slot in an NPC's daily schedule.
     * All fields come from XML specification.
     */
    public record ScheduleSlot(
            int startHour,
            int endHour,
            String activity,
            String location,
            String animation
    ) {
        public ScheduleSlot {
            if (startHour < 0 || startHour > 24) throw new IllegalArgumentException("Invalid start hour: " + startHour);
            if (endHour < 0 || endHour > 24) throw new IllegalArgumentException("Invalid end hour: " + endHour);
        }
    }
}
