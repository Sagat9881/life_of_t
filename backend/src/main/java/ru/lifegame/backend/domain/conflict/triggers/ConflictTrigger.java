package ru.lifegame.backend.domain.conflict.triggers;

import ru.lifegame.backend.domain.conflict.core.Conflict;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.relationship.Relationships;
import ru.lifegame.backend.domain.model.session.GameTime;

import java.util.Optional;

public interface ConflictTrigger {
    Optional<Conflict> check(PlayerCharacter player, Relationships relationships, GameTime time);
}
