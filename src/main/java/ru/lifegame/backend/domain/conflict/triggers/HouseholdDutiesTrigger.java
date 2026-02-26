package ru.lifegame.backend.domain.conflict.triggers;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.*;
import ru.lifegame.backend.domain.model.*;

import java.util.Optional;
import java.util.UUID;

public class HouseholdDutiesTrigger implements ConflictTrigger {
    @Override
    public Optional<Conflict> check(PlayerCharacter player, Relationships relationships, GameTime time) {
        Relationship husband = relationships.get(NpcCode.HUSBAND);
        if (husband == null || husband.broken()) {
            return Optional.empty();
        }
        
        boolean triggered = husband.closeness() < GameBalance.HUSBAND_CLOSENESS_HOUSEHOLD
            && player.daysSinceHousehold() >= GameBalance.HUSBAND_DAYS_NO_HOUSEHOLD;
        
        if (triggered) {
            return Optional.of(new Conflict(UUID.randomUUID().toString(), HusbandConflicts.HOUSEHOLD_DUTIES));
        }
        return Optional.empty();
    }
}
