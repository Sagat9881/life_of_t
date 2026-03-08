package ru.lifegame.backend.application.view;

/**
 * DTO representing a single NPC's current state for the frontend.
 * Contains everything the renderer needs to display an NPC.
 */
public record NpcActivityView(
    String npcId,
    String displayName,
    String category,
    String currentActivity,
    String currentLocation,
    String animationKey,
    int happiness,
    int energy,
    boolean isAvailableForInteraction
) {
}
