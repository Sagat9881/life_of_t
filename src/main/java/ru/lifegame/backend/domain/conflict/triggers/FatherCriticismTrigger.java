package ru.lifegame.backend.domain.conflict.triggers;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.*;
import ru.lifegame.backend.domain.model.*;

import java.util.Optional;
import java.util.UUID;

public class FatherCriticismTrigger implements ConflictTrigger {
    @Override
    public Optional<Conflict> check(PlayerCharacter player, Relationships relationships, GameTime time) {
        Relationship father = relationships.get(NpcCode.FATHER);
        if (father == null || father.broken()) {
            return Optional.empty();
        }
        
        boolean triggered = player.job().satisfaction() < GameBalance.FATHER_CRITICISM_SATISFACTION
            || player.stats().selfEsteem() < GameBalance.FATHER_CRITICISM_SELF_ESTEEM;
        
        if (triggered) {
            return Optional.of(new Conflict(UUID.randomUUID().toString(), FatherConflicts.CRITICISM_OF_CHOICES));
        }
        return Optional.empty();
    }
}
