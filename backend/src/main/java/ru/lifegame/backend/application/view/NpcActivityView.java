package ru.lifegame.backend.application.view;

/**
 * DTO representing current NPC state for the frontend.
 * Contains everything needed to render an NPC: what they're doing, where, and how to animate.
 */
public record NpcActivityView(
        String npcId,
        String displayName,
        String category,
        String currentActivity,
        String location,
        String animation,
        int happiness,
        int energy,
        boolean isAvailable
) {
}
