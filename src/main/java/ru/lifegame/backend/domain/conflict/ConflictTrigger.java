package ru.lifegame.backend.domain.conflict;

import ru.lifegame.backend.domain.model.*;

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
