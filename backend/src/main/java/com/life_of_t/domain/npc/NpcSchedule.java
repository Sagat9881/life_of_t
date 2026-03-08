package com.life_of_t.domain.npc;

import java.util.List;
import java.util.Optional;

/**
 * Data-driven NPC schedule. Slots are loaded from XML spec.
 * Three-layer resolution: base routine → mood override → quest/event override.
 * Backend knows nothing about concrete slot content — all from spec.
 */
public class NpcSchedule {

    /**
     * A time slot defining what the NPC does at a given hour range.
     * All fields are strings loaded from XML — the engine treats them as opaque IDs.
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

    private final List<ScheduleSlot> baseSlots;
    private ScheduleSlot moodOverride;
    private ScheduleSlot questOverride;

    /**
     * Created from parsed XML schedule slots.
     */
    public NpcSchedule(List<ScheduleSlot> baseSlots) {
        this.baseSlots = List.copyOf(baseSlots);
        this.moodOverride = null;
        this.questOverride = null;
    }

    /**
     * Set a mood-driven override (e.g., irritated husband goes for a walk alone).
     * Override is cleared at start of next day.
     */
    public void setMoodOverride(ScheduleSlot override) {
        this.moodOverride = override;
    }

    /**
     * Set a quest/event-driven override (e.g., NPC must be at kitchen for dinner quest).
     */
    public void setQuestOverride(ScheduleSlot override) {
        this.questOverride = override;
    }

    /**
     * Clear all overrides — called at start of each new day.
     */
    public void clearOverrides() {
        this.moodOverride = null;
        this.questOverride = null;
    }

    /**
     * Resolve what NPC should be doing at given hour.
     * Priority: quest override > mood override > base schedule.
     */
    public Optional<ScheduleSlot> resolveSlot(int currentHour) {
        if (questOverride != null && questOverride.containsHour(currentHour)) {
            return Optional.of(questOverride);
        }
        if (moodOverride != null && moodOverride.containsHour(currentHour)) {
            return Optional.of(moodOverride);
        }
        return baseSlots.stream()
                .filter(slot -> slot.containsHour(currentHour))
                .findFirst();
    }

    /**
     * Check if NPC is available for player interaction at given hour.
     * NPC is unavailable if location is "away" or activity is "sleep".
     */
    public boolean isAvailable(int currentHour) {
        return resolveSlot(currentHour)
                .map(slot -> !"away".equals(slot.locationId()) && !"sleep".equals(slot.activityId()))
                .orElse(false);
    }

    /**
     * Get the reason NPC is unavailable, if any.
     */
    public Optional<String> unavailableReason(int currentHour) {
        return resolveSlot(currentHour)
                .filter(slot -> "away".equals(slot.locationId()) || "sleep".equals(slot.activityId()))
                .map(slot -> {
                    if ("away".equals(slot.locationId())) return "is away (" + slot.activityId() + ")";
                    return "is sleeping";
                });
    }

    public List<ScheduleSlot> baseSlots() { return baseSlots; }
}
