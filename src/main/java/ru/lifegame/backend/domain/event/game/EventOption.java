package ru.lifegame.backend.domain.event.game;

import ru.lifegame.backend.domain.model.stats.StatChanges;

public record EventOption(
        String code,
        String label,
        String description,
        StatChanges statChanges,
        String relationshipNpc,
        int relationshipDelta
) {}
