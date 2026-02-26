package ru.lifegame.backend.domain.conflict.triggers;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.*;
import ru.lifegame.backend.domain.model.*;

import java.util.Optional;
import java.util.UUID;

public class LackOfAttentionTrigger implements ConflictTrigger {
    @Override
    public Optional<Conflict> check(PlayerCharacter player, Relationships relationships, GameTime time) {
        Relationship husband = relationships.get(NpcCode.HUSBAND);
        if (husband == null || husband.broken()) {
            return Optional.empty();
        }
        
        if (husband.closeness() < GameBalance.HUSBAND_CLOSENESS_ATTENTION) {
            return Optional.of(new Conflict(UUID.randomUUID().toString(), HusbandConflicts.LACK_OF_ATTENTION));
        }
        return Optional.empty();
    }
}
