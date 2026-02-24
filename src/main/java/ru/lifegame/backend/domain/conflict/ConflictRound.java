package ru.lifegame.backend.domain.conflict;

import ru.lifegame.backend.domain.model.StatChanges;
import ru.lifegame.backend.domain.model.RelationshipChanges;

public record ConflictRound(
        int roundNumber,
        String situationText,
        String tacticCode,
        String reactionText,
        CspChanges cspChanges,
        StatChanges statChanges,
        RelationshipChanges relationshipChanges,
        boolean succeeded
) {}
