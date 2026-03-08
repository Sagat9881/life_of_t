package ru.lifegame.backend.application.view;

import java.util.List;

/**
 * Aggregate DTO for GET /npc/activities endpoint.
 * Returns current state of all NPC instances in the session.
 */
public record AllNpcActivitiesView(
        int currentHour,
        int currentDay,
        int totalNpcs,
        List<NpcActivityView> npcs
) {
}
