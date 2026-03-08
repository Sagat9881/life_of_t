package com.sagat9881.lifeoft.domain.npc.model;

import java.util.*;

/**
 * Data-driven NPC schedule built from XML ScheduleSlot list.
 * No hardcoded schedules — all content from spec.
 * 
 * Supports querying current activity by hour.
 */
public class NpcSchedule {

    private final List<ScheduleSlot> slots;

    private NpcSchedule(List<ScheduleSlot> slots) {
        this.slots = List.copyOf(slots);
    }

    /**
     * Create schedule from XML-loaded slot list.
     */
    public static NpcSchedule fromSlots(List<ScheduleSlot> slots) {
        if (slots == null || slots.isEmpty()) {
            return new NpcSchedule(List.of());
        }
        return new NpcSchedule(slots);
    }

    /**
     * Get the activity for a given hour.
     * Returns first matching slot, or empty if no slot covers this hour.
     */
    public Optional<NpcActivity> getActivityAt(int hour) {
        return slots.stream()
                .filter(slot -> slot.containsHour(hour))
                .findFirst()
                .map(slot -> new NpcActivity(
                        slot.activityId(),
                        slot.animationKey(),
                        slot.locationId(),
                        slot.endHour() - hour
                ));
    }

    /**
     * Check if NPC is available (not away/sleeping) at given hour.
     */
    public boolean isAvailableAt(int hour) {
        return slots.stream()
                .filter(slot -> slot.containsHour(hour))
                .findFirst()
                .map(slot -> !"away".equals(slot.locationId())
                        && !"sleep".equals(slot.activityId())
                        && !"sleeping".equals(slot.activityId()))
                .orElse(true);
    }

    /**
     * Get location at a given hour.
     */
    public Optional<String> getLocationAt(int hour) {
        return slots.stream()
                .filter(slot -> slot.containsHour(hour))
                .findFirst()
                .map(ScheduleSlot::locationId);
    }

    public List<ScheduleSlot> allSlots() {
        return slots;
    }
}
