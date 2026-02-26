package ru.lifegame.backend.domain.conflict.triggers;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.*;
import ru.lifegame.backend.domain.model.*;

import java.util.Optional;
import java.util.UUID;

public class RomanticCrisisTrigger implements ConflictTrigger {
    @Override
    public Optional<Conflict> check(PlayerCharacter player, Relationships relationships, GameTime time) {
        Relationship husband = relationships.get(NpcCode.HUSBAND);
        if (husband == null || husband.broken()) {
            return Optional.empty();
        }
        
        boolean triggered = husband.romance() < GameBalance.HUSBAND_ROMANCE_CRISIS
            && player.consecutiveWorkDays() >= GameBalance.HUSBAND_CONSECUTIVE_WORK_DAYS;
        
        if (triggered) {
            return Optional.of(new Conflict(UUID.randomUUID().toString(), HusbandConflicts.ROMANTIC_CRISIS));
        }
        return Optional.empty();
    }
}
