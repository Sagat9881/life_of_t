package com.sagat9881.lifeoft.application.view;

/**
 * DTO for frontend: current state of a single NPC.
 * Sent via GET /npc/activities endpoint.
 * Frontend uses this to render NPC animation at correct location.
 *
 * @param npcId unique NPC identifier
 * @param displayName localized name for UI
 * @param category "human" or "animal"
 * @param activityId current activity (e.g., "breakfast", "phone_scroll", "sleeping")
 * @param animation animation key for rendering (e.g., "eating", "typing", "idle")
 * @param location where the NPC is (e.g., "kitchen", "living_room", "away")
 * @param moodSummary brief mood description for UI hints (e.g., "happy", "irritated", "lonely")
 * @param isAvailable whether NPC is available for player interaction right now
 */
public record NpcActivityView(
        String npcId,
        String displayName,
        String category,
        String activityId,
        String animation,
        String location,
        String moodSummary,
        boolean isAvailable
) {
    /**
     * Generate mood summary string from NPC mood axes.
     */
    public static String summarizeMood(double happiness, double irritability,
                                        double loneliness, double anxiety) {
        if (irritability > 70) return "irritated";
        if (anxiety > 70) return "anxious";
        if (loneliness > 60) return "lonely";
        if (happiness > 70) return "happy";
        if (happiness < 30) return "sad";
        return "neutral";
    }
}
