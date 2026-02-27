package ru.lifegame.backend.domain.action;

import java.util.Collection;

/**
 * Port for providing game actions to domain layer.
 * Implementation should be provided by infrastructure layer.
 */
public interface ActionProvider {
    
    /**
     * Get action by its code.
     * @param actionCode unique action identifier
     * @return game action instance
     * @throws ru.lifegame.backend.domain.exception.InvalidActionException if action not found
     */
    GameAction getAction(String actionCode);
    
    /**
     * Get all available actions.
     * @return collection of all game actions
     */
    Collection<GameAction> allActions();
}
