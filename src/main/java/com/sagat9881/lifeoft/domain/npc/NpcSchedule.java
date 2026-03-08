package com.sagat9881.lifeoft.domain.npc;

import com.sagat9881.lifeoft.domain.npc.spec.NpcSpec;
import com.sagat9881.lifeoft.domain.npc.spec.ScheduleSlotSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * NPC schedule built from XML spec.
 * Three-layer resolution: base routine → mood override → quest/event override.
 *
 * No hardcoded schedules — everything loaded from ScheduleSlotSpec.
 */
public class NpcSchedule {

    private final List<ScheduleSlotSpec> baseSlots;
    private final Map<String, ScheduleSlotSpec> moodOverrides;
    private ScheduleSlotSpec questOverride;

    public NpcSchedule(List<ScheduleSlotSpec> baseSlots) {
        this.baseSlots = List.copyOf(baseSlots);
        this.moodOverrides = new HashMap<>();
        this.questOverride = null;
    }

    public static NpcSchedule fromSpec(NpcSpec spec) {
        return new NpcSchedule(spec.scheduleSlots());
    }

    /**
     * Register a mood override: when dominant mood axis hits extreme,
     * this slot replaces the base schedule for those hours.
     * E.g., irritability > 70 → "walk_alone" at "park" instead of "dinner" at "kitchen".
     */
    public void registerMoodOverride(String moodAxis, ScheduleSlotSpec override) {
        moodOverrides.put(moodAxis, override);
    }

    /**
     * Set a quest/event override that takes highest priority.
     * Cleared after use.
     */
    public void setQuestOverride(ScheduleSlotSpec override) {
        this.questOverride = override;
    }

    public void clearQuestOverride() {
        this.questOverride = null;
    }

    /**
     * Resolves what the NPC should be doing at the given hour.
     * Priority: quest override > mood override > base schedule.
     *
     * @param hour current game hour (0-23)
     * @param dominantMood the dominant extreme mood axis (or null)
     * @return resolved activity slot, or empty if NPC has no activity
     */
    public Optional<ScheduleSlotSpec> resolveActivity(int hour, String dominantMood) {
        // Layer 3: quest/event override (highest priority)
        if (questOverride != null && questOverride.coversHour(hour)) {
            return Optional.of(questOverride);
        }

        // Layer 2: mood override
        if (dominantMood != null && moodOverrides.containsKey(dominantMood)) {
            ScheduleSlotSpec moodSlot = moodOverrides.get(dominantMood);
            if (moodSlot.coversHour(hour)) {
                return Optional.of(moodSlot);
            }
        }

        // Layer 1: base schedule
        return baseSlots.stream()
                .filter(slot -> slot.coversHour(hour))
                .findFirst();
    }

    /**
     * Checks if NPC is available for interaction at the given hour.
     * NPC is available if their current activity location is NOT "away".
     */
    public boolean isAvailableAt(int hour, String dominantMood) {
        return resolveActivity(hour, dominantMood)
                .map(slot -> !"away".equals(slot.locationId()))
                .orElse(false);
    }

    /**
     * Returns the unavailable reason if NPC is not available.
     */
    public String unavailableReason(int hour, String dominantMood) {
        return resolveActivity(hour, dominantMood)
                .filter(slot -> "away".equals(slot.locationId()))
                .map(slot -> slot.activityId())
                .orElse(null);
    }

    public List<ScheduleSlotSpec> baseSlots() {
        return baseSlots;
    }
}
