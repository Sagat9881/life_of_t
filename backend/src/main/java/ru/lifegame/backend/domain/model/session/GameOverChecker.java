package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.pet.Pets;
import ru.lifegame.backend.domain.model.relationship.Relationships;

import java.util.Optional;

/**
 * Domain service that checks for game over conditions.
 * Evaluates player state, relationships, and pets to determine if game should end.
 * Part of the GameSession aggregate.
 */
public class GameOverChecker {

    public Optional<GameOverReason> check(PlayerCharacter player, Relationships relationships, Pets pets) {
        if (player.isBurnedOut()) {
            return Optional.of(GameOverReason.BURNOUT);
        }
        if (relationships.isDivorced()) {
            return Optional.of(GameOverReason.DIVORCE);
        }
        if (relationships.totalCloseness() < GameBalance.ISOLATION_CLOSENESS_SUM) {
            return Optional.of(GameOverReason.ISOLATION);
        }
        if (pets.hasDeadPet()) {
            return Optional.of(GameOverReason.PET_DEATH);
        }
        if (player.isBankrupt()) {
            return Optional.of(GameOverReason.BANKRUPTCY);
        }
        return Optional.empty();
    }
}
