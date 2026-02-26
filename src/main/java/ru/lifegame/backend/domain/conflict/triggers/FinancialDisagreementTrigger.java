package ru.lifegame.backend.domain.conflict.triggers;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.*;
import ru.lifegame.backend.domain.model.*;

import java.util.Optional;
import java.util.UUID;

public class FinancialDisagreementTrigger implements ConflictTrigger {
    @Override
    public Optional<Conflict> check(PlayerCharacter player, Relationships relationships, GameTime time) {
        Relationship husband = relationships.get(NpcCode.HUSBAND);
        if (husband == null || husband.broken()) {
            return Optional.empty();
        }
        
        int money = player.stats().money();
        boolean triggered = money < GameBalance.HUSBAND_MONEY_LOW || money > GameBalance.HUSBAND_MONEY_HIGH;
        
        if (triggered) {
            return Optional.of(new Conflict(UUID.randomUUID().toString(), HusbandConflicts.FINANCIAL_DISAGREEMENT));
        }
        return Optional.empty();
    }
}
