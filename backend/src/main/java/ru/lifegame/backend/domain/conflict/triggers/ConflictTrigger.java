package ru.lifegame.backend.domain.conflict.triggers;

import ru.lifegame.backend.domain.conflict.core.Conflict;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.relationship.Relationships;
import ru.lifegame.backend.domain.model.session.GameTime;

import java.util.Optional;

/**
 * Strategy interface for conflict triggers.
 * Each implementation checks specific game conditions and returns a conflict if triggered.
 */
public interface ConflictTrigger {
    /**
     * Check if this trigger's conditions are met.
     * @return Optional containing the triggered conflict, or empty if conditions not met
     */
    Optional<Conflict> check(PlayerCharacter player, Relationships relationships, GameTime time);
}
