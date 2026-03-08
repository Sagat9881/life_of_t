package ru.lifegame.backend.application.view;

import java.util.List;

/**
 * Aggregate DTO: all NPC activities for the current game hour.
 * Returned by GET /npc/activities endpoint.
 */
public record AllNpcActivitiesView(
        int currentDay,
        int currentHour,
        List<NpcActivityView> npcs
) {
}
