package com.sagat9881.lifeoft.application.view;

/**
 * DTO sent to frontend representing an NPC's current physical state.
 * Frontend uses this to render NPC sprite at the correct location with correct animation.
 *
 * @param npcId unique NPC identifier
 * @param displayName localized name for UI
 * @param category HUMAN or ANIMAL
 * @param currentActivity what the NPC is doing (e.g. "eating", "sleeping", "working")
 * @param animationKey animation to play (e.g. "eating", "typing", "idle")
 * @param locationId where the NPC is (e.g. "kitchen", "living_room", "away")
 * @param moodSummary simplified mood for visual cues (e.g. "happy", "sad", "irritated", "neutral")
 * @param isAvailable whether NPC can be interacted with right now
 */
public record NpcActivityView(
        String npcId,
        String displayName,
        String category,
        String currentActivity,
        String animationKey,
        String locationId,
        String moodSummary,
        boolean isAvailable
) {

    /**
     * Derives a simplified mood label from dominant mood axis.
     */
    public static String deriveMoodSummary(int happiness, int irritability, int loneliness, int anxiety) {
        if (irritability > 60) return "irritated";
        if (anxiety > 60) return "anxious";
        if (loneliness > 60) return "lonely";
        if (happiness > 60) return "happy";
        if (happiness < 30) return "sad";
        return "neutral";
    }
}
