package ru.lifegame.backend.domain.conflict;

import ru.lifegame.backend.domain.model.StatChanges;
import ru.lifegame.backend.domain.model.RelationshipChanges;

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
