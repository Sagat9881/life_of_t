package ru.lifegame.backend.application.view;

import java.util.List;

public record AllNpcActivitiesView(
        int gameDay,
        int gameHour,
        List<NpcActivityView> npcActivities
) {
}
