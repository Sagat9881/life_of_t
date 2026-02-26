package ru.lifegame.backend.domain.conflict.triggers;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.*;
import ru.lifegame.backend.domain.model.*;

import java.util.Optional;
import java.util.UUID;

public class BurnoutTrigger implements ConflictTrigger {
    @Override
    public Optional<Conflict> check(PlayerCharacter player, Relationships relationships, GameTime time) {
        boolean triggered = player.job().burnoutRisk() > GameBalance.INTERNAL_BURNOUT_RISK
            || player.stats().stress() > GameBalance.INTERNAL_BURNOUT_STRESS;
        
        if (triggered) {
            return Optional.of(new Conflict(UUID.randomUUID().toString(), InternalConflicts.BURNOUT));
        }
        return Optional.empty();
    }
}
