package com.sagat9881.lifeoft.domain.npc.spec;

import com.sagat9881.lifeoft.domain.npc.model.NpcSchedule;

import java.util.List;
import java.util.Map;

/**
 * Immutable NPC specification loaded from XML.
 * Contains all static configuration for an NPC — no game logic.
 * The engine creates NpcInstance from this spec at session start.
 *
 * @param id unique NPC identifier (e.g., "alexander", "sam", "neighbor_cat")
 * @param type "named" (full brain) or "filler" (light brain)
 * @param category "human" or "animal"
 * @param displayName localized display name for UI
 * @param personalityTraits Map of trait name → value (0-100), e.g., patience=60, humor=45
 * @param moodInitial Map of mood axis → initial value, e.g., happiness=70, energy=80
 * @param memoryEnabled whether this NPC tracks player actions (named=true, filler=false)
 * @param shortTermSize capacity of short-term memory ring buffer
 * @param scheduleSlots daily schedule time slots from XML
 * @param actions scored actions this NPC can perform (Utility AI candidates)
 */
public record NpcSpec(
        String id,
        String type,
        String category,
        String displayName,
        Map<String, Double> personalityTraits,
        Map<String, Double> moodInitial,
        boolean memoryEnabled,
        int shortTermSize,
        List<NpcSchedule.ScheduleSlot> scheduleSlots,
        List<ScoredAction> actions
) {
    public NpcSpec {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("NPC id is required");
        if (type == null) type = "filler";
        if (category == null) category = "human";
        if (displayName == null) displayName = id;
        if (personalityTraits == null) personalityTraits = Map.of();
        if (moodInitial == null) moodInitial = Map.of();
        if (scheduleSlots == null) scheduleSlots = List.of();
        if (actions == null) actions = List.of();
    }

    public boolean isNamed() {
        return "named".equals(type);
    }

    public boolean isAnimal() {
        return "animal".equals(category);
    }

    public double trait(String name) {
        return personalityTraits.getOrDefault(name, 50.0);
    }
}
