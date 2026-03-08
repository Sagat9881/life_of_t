package ru.lifegame.backend.application.view;

import java.util.List;

/**
 * Aggregate DTO for GET /npc/activities endpoint.
 * Returns current state of all NPC in the game session.
 */
public record AllNpcActivitiesView(
        int day,
        int hour,
        List<NpcActivityView> npcs
) {
}
