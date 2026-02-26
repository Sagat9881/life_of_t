package ru.lifegame.backend.domain.conflict.core;

import ru.lifegame.backend.domain.model.stats.StatChanges;
import ru.lifegame.backend.domain.model.relationship.RelationshipChanges;

public record ConflictRound(
        int roundNumber,
        String situation,
        String tacticCode,
        String reactionText,
        CspChanges cspChanges,
        StatChanges statChanges,
        RelationshipChanges relationshipChanges,
        boolean succeeded
) {}
