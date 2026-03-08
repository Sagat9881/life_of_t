package com.sagat9881.lifeoft.domain.npc;

/**
 * Current physical activity of an NPC.
 * Describes what the NPC is doing, their animation, and location.
 * All values come from XML specification or Utility AI decisions.
 *
 * @param activityId   semantic name: "breakfast", "sleeping", "phone_scroll", "roaming"
 * @param animationKey animation to play: "eating", "sleeping", "typing", "walking"
 * @param locationId   where in the scene: "kitchen", "bedroom", "sofa", "away"
 */
public record NpcActivity(
        String activityId,
        String animationKey,
        String locationId
) {
    /**
     * Default idle activity.
     */
    public static NpcActivity idle() {
        return new NpcActivity("idle", "idle", "home");
    }

    /**
     * Check if NPC is away (not visible in scene).
     */
    public boolean isAway() {
        return "away".equals(locationId);
    }

    /**
     * Check if NPC is sleeping.
     */
    public boolean isSleeping() {
        return "sleeping".equals(activityId) || "sleep".equals(activityId);
    }
}
