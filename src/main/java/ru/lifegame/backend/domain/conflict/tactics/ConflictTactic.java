package ru.lifegame.backend.domain.conflict.tactics;

import ru.lifegame.backend.domain.conflict.core.*;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.relationship.Relationships;

import java.util.Optional;

public interface ConflictTactic {
    String code();
    String label();
    String description();
    boolean isBaseAvailable();
    Optional<String> requiredSkill();
    int requiredSkillLevel();
    String defaultReactionText();
    TacticEffects calculateEffects(PlayerCharacter player, Conflict conflict, Relationships relationships);
}
