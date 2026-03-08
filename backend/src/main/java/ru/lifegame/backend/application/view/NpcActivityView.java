package ru.lifegame.backend.application.view;

/**
 * DTO representing a single NPC's current visual state for the frontend.
 * Contains everything needed to render the NPC in the scene.
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
     * Creates a view with a computed mood summary string.
     */
    public static NpcActivityView of(
            String npcId,
            String displayName,
            String category,
            String currentActivity,
            String animation,
            String location,
            int happiness,
            int irritability) {

        String mood;
        if (irritability > 60) {
            mood = "irritated";
        } else if (happiness > 70) {
            mood = "happy";
        } else if (happiness < 30) {
            mood = "sad";
        } else {
            mood = "neutral";
        }

        return new NpcActivityView(npcId, displayName, category,
                currentActivity, animation, location, mood);
    }
}
