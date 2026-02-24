package ru.lifegame.backend.domain.conflict;

import ru.lifegame.backend.domain.model.PlayerCharacter;
import ru.lifegame.backend.domain.model.Relationships;
import ru.lifegame.backend.domain.model.StatChanges;
import ru.lifegame.backend.domain.model.RelationshipChanges;

public record TacticEffects(
        CspChanges cspChanges,
        StatChanges statChanges,
        RelationshipChanges relationshipChanges,
        boolean succeeded,
        String reactionText
) {}
