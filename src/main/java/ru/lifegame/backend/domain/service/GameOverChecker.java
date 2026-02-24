package ru.lifegame.backend.domain.service;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.*;

import java.util.Optional;

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
