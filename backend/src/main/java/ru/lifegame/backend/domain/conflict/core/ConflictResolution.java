package ru.lifegame.backend.domain.conflict.core;

import ru.lifegame.backend.domain.model.relationship.RelationshipChanges;
import ru.lifegame.backend.domain.model.stats.StatChanges;

public record ConflictResolution(
        String outcome,
        StatChanges statChanges,
        RelationshipChanges relationshipChanges,
        boolean relationshipBreak
) {
    public static ConflictResolution avoided() {
        return new ConflictResolution("AVOIDED", StatChanges.none(), null, false);
    }

    public static ConflictResolution fromOutcome(String outcome) {
        return new ConflictResolution(outcome, StatChanges.none(), null, false);
    }

    public static ConflictResolution withBreak(String outcome) {
        return new ConflictResolution(outcome, StatChanges.none(), null, true);
    }
}
