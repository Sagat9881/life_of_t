package ru.lifegame.backend.infrastructure.game;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.exception.InvalidActionException;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Infrastructure adapter that provides game actions to the domain.
 * Maintains a registry of all available actions.
 */
public class GameEngineAdapter implements ActionProvider {
    
    private final Map<String, GameAction> actions;

    public GameEngineAdapter(Collection<GameAction> actions) {
        this.actions = actions.stream()
                .collect(Collectors.toMap(a -> a.type().code(), Function.identity()));
    }

    @Override
    public GameAction getAction(String actionCode) {
        GameAction action = actions.get(actionCode);
        if (action == null) {
            throw new InvalidActionException("Unknown action: " + actionCode);
        }
        return action;
    }

    @Override
    public Collection<GameAction> allActions() {
        return actions.values();
    }
}
