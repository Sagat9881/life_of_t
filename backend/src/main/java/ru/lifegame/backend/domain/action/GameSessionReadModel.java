package ru.lifegame.backend.domain.action;

import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.pet.Pets;
import ru.lifegame.backend.domain.model.relationship.Relationships;
import ru.lifegame.backend.domain.model.session.GameTime;

/**
 * Read-only view of GameSession for action calculations.
 */
public interface GameSessionReadModel {
    PlayerCharacter player();
    Relationships relationships();
    Pets pets();
    GameTime time();
}
