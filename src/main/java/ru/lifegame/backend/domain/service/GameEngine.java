package ru.lifegame.backend.domain.service;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.exception.InvalidActionException;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GameEngine {
    private final Map<String, GameAction> actions;

    public GameEngine(Collection<GameAction> actions) {
        this.actions = actions.stream()
                .collect(Collectors.toMap(a -> a.type().code(), Function.identity()));
    }

    public GameAction getAction(String actionCode) {
        GameAction action = actions.get(actionCode);
        if (action == null) {
            throw new InvalidActionException("Unknown action: " + actionCode);
        }
        return action;
    }

    public Collection<GameAction> allActions() {
        return actions.values();
    }
}
