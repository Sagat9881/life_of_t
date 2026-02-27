package ru.lifegame.backend.domain.action;

import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

public record ActionResult(
        ActionType actionType,
        int timeCost,
        String description,
        StatChanges statChanges,
        Map<String, Integer> relationshipChanges,
        Map<String, Integer> petMoodChanges,
        boolean rested,
        boolean workedToday,
        boolean interactedWithHusband,
        boolean interactedWithFather
) {}
