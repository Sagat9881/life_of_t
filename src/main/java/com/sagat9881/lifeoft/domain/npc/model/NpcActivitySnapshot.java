package com.sagat9881.lifeoft.domain.npc.model;

/**
 * Immutable snapshot of an NPC's current activity for frontend rendering.
 * Contains everything the client needs to display the NPC.
 */
public record NpcActivitySnapshot(
        String npcId,
        String displayName,
        String category,
        String activityId,
        String animationKey,
        String locationId
) {
}
