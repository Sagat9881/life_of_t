package com.sagat9881.lifeoft.domain.npc.model;

import java.util.*;

/**
 * Immutable specification of an NPC, loaded from XML.
 * The engine creates NpcInstance from this spec.
 * 
 * Contains all static data: personality, schedule template,
 * available actions, mood config, memory config.
 * 
 * No game state here — only the "blueprint" of the NPC.
 */
public record NpcSpec(
        String id,
        String type,           // "named" or "filler"
        String category,       // "human", "cat", "dog"
        String displayName,
        String defaultLocation,
        Map<String, Double> personality,
        Map<String, Double> moodInitial,
        Map<String, Double> moodDecayRates,
        boolean memoryEnabled,
        int shortTermSize,
        List<ScheduleSlot> scheduleSlots,
        List<ScoredAction> scoredActions,
        List<ActionReaction> actionReactions
) {

    public NpcSpec {
        personality = personality != null ? Map.copyOf(personality) : Map.of();
        moodInitial = moodInitial != null ? Map.copyOf(moodInitial) : Map.of();
        moodDecayRates = moodDecayRates != null ? Map.copyOf(moodDecayRates) : Map.of();
        scheduleSlots = scheduleSlots != null ? List.copyOf(scheduleSlots) : List.of();
        scoredActions = scoredActions != null ? List.copyOf(scoredActions) : List.of();
        actionReactions = actionReactions != null ? List.copyOf(actionReactions) : List.of();
    }

    /**
     * Get personality trait value (0-100). Returns 50 if not defined.
     */
    public double personalityTrait(String trait) {
        return personality.getOrDefault(trait, 50.0);
    }

    /**
     * Reaction to a specific player action.
     */
    public record ActionReaction(
            String triggerActionId,
            Map<String, Double> moodChanges
    ) {
        public ActionReaction {
            moodChanges = moodChanges != null ? Map.copyOf(moodChanges) : Map.of();
        }
    }
}
