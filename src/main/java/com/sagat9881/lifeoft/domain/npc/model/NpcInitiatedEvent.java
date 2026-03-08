package com.sagat9881.lifeoft.domain.npc.model;

import java.util.*;

/**
 * An event initiated by an NPC through Utility AI.
 * Converted to GameEvent by the event engine for player interaction.
 * 
 * Contains NPC source, event type, and player response options.
 */
public record NpcInitiatedEvent(
        String sourceNpcId,
        String actionId,
        String eventType,
        List<Map<String, String>> options
) {

    public NpcInitiatedEvent {
        options = options != null ? List.copyOf(options) : List.of();
    }
}
