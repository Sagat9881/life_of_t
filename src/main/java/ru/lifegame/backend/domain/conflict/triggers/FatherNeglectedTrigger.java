package ru.lifegame.backend.domain.conflict.triggers;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.*;
import ru.lifegame.backend.domain.model.*;

import java.util.Optional;
import java.util.UUID;

public class FatherNeglectedTrigger implements ConflictTrigger {
    @Override
    public Optional<Conflict> check(PlayerCharacter player, Relationships relationships, GameTime time) {
        Relationship father = relationships.get(NpcCode.FATHER);
        if (father == null || father.broken()) {
            return Optional.empty();
        }
        
        if (father.closeness() < GameBalance.FATHER_CLOSENESS_NEGLECTED) {
            return Optional.of(new Conflict(UUID.randomUUID().toString(), FatherConflicts.FEELING_NEGLECTED));
        }
        return Optional.empty();
    }
}
