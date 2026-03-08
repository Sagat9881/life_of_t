package ru.lifegame.backend.application.view;

import java.util.List;

/**
 * Aggregated view of all NPC activities for a given game hour.
 * Frontend calls GET /npc/activities to get this.
 */
public record AllNpcActivitiesView(
        int currentDay,
        int currentHour,
        List<NpcActivityView> npcs
) {
}
