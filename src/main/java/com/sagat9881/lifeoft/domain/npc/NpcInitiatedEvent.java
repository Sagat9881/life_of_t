package com.sagat9881.lifeoft.domain.npc;

import java.util.List;

/**
 * An event initiated by an NPC through the Utility AI system.
 * Bridges between NPC behavior engine and the game event system.
 * All content comes from XML — the backend only provides the structure.
 */
public record NpcInitiatedEvent(
        String npcId,
        String actionId,
        String eventType,
        List<NpcSpecLoader.ActionOption> options
) {
    /**
     * Create from a scored action selected by Utility AI.
     */
    public static NpcInitiatedEvent fromScoredAction(String npcId, ScoredAction action) {
        return new NpcInitiatedEvent(
                npcId,
                action.actionId(),
                action.eventType(),
                action.options()
        );
    }

    /**
     * Check if this event has player choices.
     */
    public boolean hasOptions() {
        return options != null && !options.isEmpty();
    }
}
