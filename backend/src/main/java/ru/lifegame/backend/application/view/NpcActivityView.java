package ru.lifegame.backend.application.view;

import java.util.Map;

public record NpcActivityView(
        String npcId,
        String displayName,
        String category,
        String currentActivity,
        String currentLocation,
        String animationKey,
        Map<String, Integer> moodSnapshot
) {
}
