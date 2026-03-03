package ru.lifegame.backend.domain.action;

import java.util.Collection;

/**
 * Port for providing game actions to domain layer.
 */
public interface ActionProvider {
    GameAction getAction(String actionCode);
    Collection<GameAction> allActions();
}
