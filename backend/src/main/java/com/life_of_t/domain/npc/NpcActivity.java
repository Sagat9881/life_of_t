package com.life_of_t.domain.npc;

/**
 * Current physical activity of an NPC.
 * All fields are opaque string IDs loaded from XML — the engine
 * does not know what "eating" or "kitchen" means, only the frontend does.
 */
public record NpcActivity(
        String activityId,
        String animationKey,
        String locationId
) {
    /**
     * Default idle activity when no schedule slot is active.
     */
    public static NpcActivity idle(String defaultLocation) {
        return new NpcActivity("idle", "idle", defaultLocation);
    }
}
