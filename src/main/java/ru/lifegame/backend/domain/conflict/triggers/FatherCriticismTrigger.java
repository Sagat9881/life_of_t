package ru.lifegame.backend.domain.conflict.triggers;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.*;
import ru.lifegame.backend.domain.conflict.core.Conflict;
import ru.lifegame.backend.domain.conflict.types.FatherConflicts;
import ru.lifegame.backend.domain.model.*;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.relationship.NpcCode;
import ru.lifegame.backend.domain.model.relationship.Relationship;
import ru.lifegame.backend.domain.model.relationship.Relationships;
import ru.lifegame.backend.domain.model.session.GameTime;

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
