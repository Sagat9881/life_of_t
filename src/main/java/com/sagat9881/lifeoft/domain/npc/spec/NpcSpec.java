package com.sagat9881.lifeoft.domain.npc.spec;

import java.util.List;
import java.util.Map;

/**
 * Immutable specification of an NPC loaded from XML.
 * The engine never hardcodes NPC names — all content comes from specs.
 *
 * @param id unique identifier matching XML id attribute
 * @param type NAMED (full brain) or FILLER (simplified brain)
 * @param category HUMAN or ANIMAL
 * @param displayName localized display name
 * @param personalityTraits trait name → value (0-100)
 * @param moodInitial axis name → initial value
 * @param memoryEnabled whether NPC tracks player actions
 * @param shortTermMemorySize max short-term memory entries (only if memoryEnabled)
 * @param scheduleSlots time-based activity schedule
 * @param actions available utility-scored actions
 * @param questLines list of quest IDs this NPC participates in
 */
public record NpcSpec(
        String id,
        NpcType type,
        NpcCategory category,
        String displayName,
        Map<String, Integer> personalityTraits,
        Map<String, Integer> moodInitial,
        boolean memoryEnabled,
        int shortTermMemorySize,
        List<ScheduleSlotSpec> scheduleSlots,
        List<ScoredAction> actions,
        List<String> questLines
) {

    public enum NpcType {
        NAMED, FILLER
    }

    public enum NpcCategory {
        HUMAN, ANIMAL
    }

    public boolean isNamed() {
        return type == NpcType.NAMED;
    }

    public boolean isFiller() {
        return type == NpcType.FILLER;
    }

    public int personalityTrait(String name) {
        return personalityTraits.getOrDefault(name, 50);
    }

    public int initialMood(String axis) {
        return moodInitial.getOrDefault(axis, 50);
    }
}
