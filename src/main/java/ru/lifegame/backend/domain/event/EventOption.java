package ru.lifegame.backend.domain.event;

import ru.lifegame.backend.domain.model.StatChanges;

public record EventOption(
        String code,
        String label,
        String description,
        StatChanges statChanges,
        String relationshipNpc,
        int relationshipDelta
) {}
