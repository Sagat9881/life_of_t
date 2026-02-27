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
    // Compatibility methods for view mapper
    public String code() { return id; }
    public String label() { return text; }
    public String description() { return text; }
}
