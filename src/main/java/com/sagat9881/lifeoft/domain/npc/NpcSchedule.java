package com.sagat9881.lifeoft.domain.npc;

import java.util.Collections;
import java.util.List;

/**
 * Data-driven NPC schedule loaded from XML specification.
 * No hardcoded schedules — all time slots come from narrative XML.
 * Supports mood-based overrides (handled by NpcLifecycleEngine).
 */
public class NpcSchedule {

    private final List<NpcSpecLoader.NpcScheduleSlot> slots;

    private NpcSchedule(List<NpcSpecLoader.NpcScheduleSlot> slots) {
        this.slots = slots;
    }

    /**
     * Create schedule from XML-parsed slots.
     */
    public static NpcSchedule fromSlots(List<NpcSpecLoader.NpcScheduleSlot> slots) {
        return new NpcSchedule(slots != null ? slots : Collections.emptyList());
    }

    /**
     * Get the activity for a given hour of day.
     * Returns the first matching slot, or a default idle activity.
     */
    public NpcActivity activityAt(int hour) {
        for (NpcSpecLoader.NpcScheduleSlot slot : slots) {
            if (hour >= slot.startHour() && hour < slot.endHour()) {
                return new NpcActivity(
                        slot.activity(),
                        slot.animation(),
                        slot.location()
                );
            }
        }
        return new NpcActivity("idle", "idle", "home");
    }

    /**
     * Check if NPC is available (not away, not sleeping) at given hour.
     */
    public boolean isAvailableAt(int hour) {
        NpcActivity activity = activityAt(hour);
        return !"away".equals(activity.locationId())
                && !"sleep".equals(activity.activityId());
    }

    /**
     * Get the reason NPC is unavailable, or null if available.
     */
    public String unavailableReasonAt(int hour) {
        NpcActivity activity = activityAt(hour);
        if ("away".equals(activity.locationId())) {
            return "At " + activity.activityId();
        }
        if ("sleep".equals(activity.activityId())) {
            return "Sleeping";
        }
        return null;
    }

    public List<NpcSpecLoader.NpcScheduleSlot> slots() {
        return Collections.unmodifiableList(slots);
    }
}
