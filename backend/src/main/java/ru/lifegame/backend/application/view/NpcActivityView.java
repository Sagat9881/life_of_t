package ru.lifegame.backend.application.view;

/**
 * DTO representing an NPC's current state for the frontend.
 * Contains everything the renderer needs to display the NPC.
 */
public record NpcActivityView(
    String npcId,
    String displayName,
    String category,
    String currentActivity,
    String animation,
    String location,
    int happiness,
    int energy,
    int loneliness,
    int irritability
) {
}
