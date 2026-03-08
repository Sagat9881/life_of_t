package ru.lifegame.backend.application.view;

/**
 * DTO representing the current activity state of a single NPC.
 * Sent to frontend for rendering NPC animations and positions.
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
        boolean isAvailable
) {
}
