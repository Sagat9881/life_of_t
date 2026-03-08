package ru.lifegame.backend.application.view;

/**
 * DTO representing current NPC state for frontend rendering.
 * Contains everything the client needs to display the NPC.
 */
public record NpcActivityView(
        String npcId,
        String displayName,
        String category,
        String currentActivity,
        String animation,
        String location,
        int happiness,
        int energy
) {
}
