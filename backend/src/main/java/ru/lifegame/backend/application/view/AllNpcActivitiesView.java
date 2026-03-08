package ru.lifegame.backend.application.view;

import java.util.List;

/**
 * Aggregate DTO containing current activities of all NPC in the scene.
 * Returned by GET /npc/activities endpoint.
 */
public record AllNpcActivitiesView(
        int currentHour,
        int currentDay,
        List<NpcActivityView> npcs
) {

    public static AllNpcActivitiesView of(int hour, int day, List<NpcActivityView> npcs) {
        return new AllNpcActivitiesView(hour, day, npcs);
    }

    public int npcCount() {
        return npcs.size();
    }
}
