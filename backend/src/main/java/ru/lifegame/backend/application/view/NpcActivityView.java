package ru.lifegame.backend.application.view;

import java.util.Map;

/**
 * DTO representing current NPC state for frontend rendering.
 * Contains everything the client needs to display and animate an NPC.
 */
public record NpcActivityView(
    String npcId,
    String displayName,
    String category,
    String currentActivity,
    String currentLocation,
    String animation,
    Map<String, Integer> mood,
    boolean isAvailableForInteraction
) {
}
