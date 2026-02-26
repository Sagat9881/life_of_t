package ru.lifegame.backend.domain.event;

import ru.lifegame.backend.domain.model.stats.StatChanges;

public record EventResult(
        String description,
        StatChanges statChanges,
        String relationshipNpc,
        int relationshipDelta
) {}
