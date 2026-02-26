package ru.lifegame.backend.domain.conflict.tactics;

import ru.lifegame.backend.domain.conflict.core.CspChanges;
import ru.lifegame.backend.domain.model.stats.StatChanges;
import ru.lifegame.backend.domain.model.relationship.RelationshipChanges;

public record TacticEffects(
        CspChanges cspChanges,
        StatChanges statChanges,
        RelationshipChanges relationshipChanges,
        boolean succeeded,
        String reactionText
) {}
