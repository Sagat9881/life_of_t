package com.sagat9881.lifeoft.domain.npc;

/**
 * DTO for frontend rendering of NPC state.
 * Contains everything the client needs to display an NPC:
 * who they are, what they're doing, where, and what animation to play.
 */
public record NpcActivityView(
        String npcId,
        String displayName,
        String category,
        String currentActivity,
        String animation,
        String location,
        String moodSummary
) {
    /**
     * Create view from a live NPC instance.
     */
    public static NpcActivityView from(NpcInstance npc) {
        NpcActivity activity = npc.currentActivity();
        return new NpcActivityView(
                npc.spec().id(),
                npc.spec().displayName(),
                npc.spec().category(),
                activity != null ? activity.activityId() : "idle",
                activity != null ? activity.animationKey() : "idle",
                activity != null ? activity.locationId() : "unknown",
                describeMood(npc.mood())
        );
    }

    /**
     * Human-readable mood summary for UI display.
     * Returns the dominant mood axis.
     */
    private static String describeMood(NpcMood mood) {
        if (mood.irritability() > 70) return "irritated";
        if (mood.loneliness() > 70) return "lonely";
        if (mood.anxiety() > 70) return "anxious";
        if (mood.energy() < 20) return "exhausted";
        if (mood.happiness() > 70) return "happy";
        if (mood.affection() > 70) return "loving";
        return "neutral";
    }
}
