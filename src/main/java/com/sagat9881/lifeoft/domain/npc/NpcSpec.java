package com.sagat9881.lifeoft.domain.npc;

import java.util.List;
import java.util.Map;

/**
 * Immutable specification of an NPC, loaded from XML.
 * The backend operates on this abstraction — it knows nothing
 * about specific characters. All personality, schedule, and
 * behavior data comes from narrative XML files.
 *
 * @param id             unique NPC identifier (e.g. "alexander", "sam")
 * @param type           "named" (full brain) or "filler" (simplified)
 * @param category       "human" or "animal"
 * @param displayName    human-readable name for UI
 * @param personalityTraits  Map of trait name → value (0-100)
 * @param moodInitial    initial mood values from XML
 * @param memoryEnabled  whether this NPC tracks player actions
 * @param shortTermSize  max short-term memory entries
 * @param scheduleSlots  daily schedule from XML
 * @param actions        available NPC-initiated actions with scoring
 */
public record NpcSpec(
        String id,
        String type,
        String category,
        String displayName,
        Map<String, Integer> personalityTraits,
        NpcSpecLoader.NpcMoodInitial moodInitial,
        boolean memoryEnabled,
        int shortTermSize,
        List<NpcSpecLoader.NpcScheduleSlot> scheduleSlots,
        List<ScoredAction> actions
) {
    public boolean isNamed() {
        return "named".equals(type);
    }

    public boolean isFiller() {
        return "filler".equals(type);
    }

    public boolean isHuman() {
        return "human".equals(category);
    }

    public boolean isAnimal() {
        return "animal".equals(category);
    }

    public int traitValue(String traitName, int defaultValue) {
        return personalityTraits != null
                ? personalityTraits.getOrDefault(traitName, defaultValue)
                : defaultValue;
    }
}
