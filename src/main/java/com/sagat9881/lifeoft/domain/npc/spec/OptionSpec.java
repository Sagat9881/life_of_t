package com.sagat9881.lifeoft.domain.npc.spec;

import java.util.Map;

/**
 * A player-facing option within an NPC-initiated event, loaded from XML.
 * All stat changes and relationship effects are data-driven.
 *
 * @param optionId unique option identifier
 * @param text display text for the player
 * @param resultText narrative result description
 * @param statChanges stat name → delta (e.g. "stress" → -10, "mood" → 15)
 * @param relationshipTarget NPC id affected by this choice (nullable)
 * @param relationshipDelta closeness change for the target NPC
 * @param romanceDelta romance change (only for romantic NPCs)
 * @param trustDelta trust change
 * @param skillChanges skill name → delta (e.g. "empathy" → 2)
 */
public record OptionSpec(
        String optionId,
        String text,
        String resultText,
        Map<String, Integer> statChanges,
        String relationshipTarget,
        int relationshipDelta,
        int romanceDelta,
        int trustDelta,
        Map<String, Integer> skillChanges
) {

    public boolean affectsRelationship() {
        return relationshipTarget != null && !relationshipTarget.isBlank();
    }

    public int statChange(String stat) {
        return statChanges != null ? statChanges.getOrDefault(stat, 0) : 0;
    }

    public int skillChange(String skill) {
        return skillChanges != null ? skillChanges.getOrDefault(skill, 0) : 0;
    }
}
