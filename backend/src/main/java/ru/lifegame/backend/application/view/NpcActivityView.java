package ru.lifegame.backend.application.view;

/**
 * DTO representing the current state of a single NPC for the frontend.
 * Contains everything needed to render the NPC in the scene.
 */
public record NpcActivityView(
        String npcId,
        String displayName,
        String type,
        String category,
        String currentActivity,
        String currentLocation,
        String animation,
        int happiness,
        int energy,
        boolean isAvailableForInteraction
) {
}
