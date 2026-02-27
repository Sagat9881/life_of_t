package ru.lifegame.backend.domain.event.game;

import ru.lifegame.backend.domain.model.stats.StatChanges;
import java.util.Map;

public record EventOption(
    String id,
    String text,
    String resultText,
    StatChanges statChanges,
    Map<String, Integer> relationshipChanges
) {
}
