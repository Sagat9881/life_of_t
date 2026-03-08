package com.sagat9881.lifeoft.domain.npc.model;

/**
 * Current physical activity of an NPC.
 * Represents what the NPC is doing right now: sitting, walking, eating, phone, etc.
 * Used by frontend to render NPC animation and position.
 * All values come from XML spec or Utility AI decision.
 */
public record NpcActivity(
        String activityId,
        String animation,
        String location
) {
    public static NpcActivity idle(String location) {
        return new NpcActivity("idle", "idle", location);
    }
}
