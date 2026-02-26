package ru.lifegame.backend.domain.conflict.triggers;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.*;
import ru.lifegame.backend.domain.model.*;

import java.util.Optional;
import java.util.UUID;

public class IdentityCrisisTrigger implements ConflictTrigger {
    @Override
    public Optional<Conflict> check(PlayerCharacter player, Relationships relationships, GameTime time) {
        boolean triggered = player.stats().selfEsteem() < GameBalance.INTERNAL_IDENTITY_SELF_ESTEEM
            && player.job().satisfaction() < GameBalance.INTERNAL_IDENTITY_SATISFACTION;
        
        if (triggered) {
            return Optional.of(new Conflict(UUID.randomUUID().toString(), InternalConflicts.IDENTITY_CRISIS));
        }
        return Optional.empty();
    }
}
