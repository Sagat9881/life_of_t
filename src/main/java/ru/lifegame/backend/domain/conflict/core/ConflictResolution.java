package ru.lifegame.backend.domain.conflict.core;

import ru.lifegame.backend.domain.model.stats.StatChanges;
import ru.lifegame.backend.domain.model.relationship.RelationshipChanges;

public record ConflictResolution(
        ConflictOutcome outcome,
        StatChanges statChanges,
        RelationshipChanges relationshipChanges,
        boolean relationshipBreak
) {
    public static ConflictResolution avoided() {
        return new ConflictResolution(ConflictOutcome.AVOIDED, StatChanges.none(), null, false);
    }

    public static ConflictResolution fromOutcome(ConflictOutcome outcome) {
        return new ConflictResolution(outcome, StatChanges.none(), null, false);
    }

    public static ConflictResolution withBreak(ConflictOutcome outcome) {
        return new ConflictResolution(outcome, StatChanges.none(), null, true);
    }
}
