package ru.lifegame.backend.domain.action;

import ru.lifegame.backend.domain.model.*;

/**
 * Read-only view of GameSession for action calculations.
 */
public interface GameSessionReadModel {
    PlayerCharacter player();
    Relationships relationships();
    Pets pets();
    GameTime time();
}
