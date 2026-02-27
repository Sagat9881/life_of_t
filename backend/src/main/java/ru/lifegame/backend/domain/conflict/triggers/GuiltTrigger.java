package ru.lifegame.backend.domain.conflict.triggers;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.core.Conflict;
import ru.lifegame.backend.domain.conflict.types.InternalConflicts;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.relationship.Relationships;
import ru.lifegame.backend.domain.model.session.GameTime;

import java.util.Optional;
import java.util.UUID;

public class GuiltTrigger implements ConflictTrigger {
    @Override
    public Optional<Conflict> check(PlayerCharacter player, Relationships relationships, GameTime time) {
        boolean triggered = relationships.totalCloseness() < GameBalance.INTERNAL_GUILT_CLOSENESS_SUM
            && player.stats().selfEsteem() < GameBalance.INTERNAL_GUILT_SELF_ESTEEM;
        
        if (triggered) {
            return Optional.of(new Conflict(UUID.randomUUID().toString(), InternalConflicts.GUILT));
        }
        return Optional.empty();
    }
}
