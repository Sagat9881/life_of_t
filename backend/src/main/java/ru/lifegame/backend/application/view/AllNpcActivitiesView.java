package ru.lifegame.backend.application.view;

import java.util.List;

/**
 * Aggregate DTO: all NPC activities for a single API response.
 * Used by GET /npc/activities endpoint.
 */
public record AllNpcActivitiesView(
    int currentHour,
    int currentDay,
    List<NpcActivityView> npcs
) {
}
