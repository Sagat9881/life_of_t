package ru.lifegame.backend.domain.conflict;

import ru.lifegame.backend.domain.model.*;

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
