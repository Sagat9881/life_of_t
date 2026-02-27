package ru.lifegame.backend.domain.event.game;

import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

public record EventResult(
    String message,
    StatChanges statChanges,
    Map<String, Integer> relationshipChanges,
    String questUpdate
) {
}
