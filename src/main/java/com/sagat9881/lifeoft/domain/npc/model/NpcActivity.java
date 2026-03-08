package com.sagat9881.lifeoft.domain.npc.model;

/**
 * Current physical activity of an NPC.
 * Used by frontend to render NPC animation and position.
 * 
 * All fields are strings — the engine doesn't interpret them.
 * Animation keys and location IDs are defined in XML specs.
 * 
 * @param activityId what the NPC is doing (eating, sleeping, walking, phone, etc.)
 * @param animationKey animation to play on frontend (maps to sprite/animation)
 * @param locationId where the NPC is (kitchen, living_room, yard, away, etc.)
 * @param remainingHours how long this activity lasts (0 = indefinite)
 */
public record NpcActivity(
        String activityId,
        String animationKey,
        String locationId,
        int remainingHours
) {

    /**
     * Default idle activity at a given location.
     */
    public static NpcActivity idle(String locationId) {
        return new NpcActivity("idle", "idle", locationId, 0);
    }

    /**
     * Check if this is the default idle state.
     */
    public boolean isIdle() {
        return "idle".equals(activityId);
    }
}
