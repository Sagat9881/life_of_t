package ru.lifegame.backend.domain.action.spec;

import ru.lifegame.backend.domain.action.ActionProvider;
import ru.lifegame.backend.domain.action.GameAction;
import ru.lifegame.backend.domain.exception.InvalidActionException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Data-driven implementation of ActionProvider.
 * Loads all actions from XML specs via PlayerActionSpecLoader
 * and provides them to the domain layer.
 */
public class DataDrivenActionProvider implements ActionProvider {

    private final Map<String, GameAction> actionsByCode;
    private final List<GameAction> allActions;

    public DataDrivenActionProvider(PlayerActionSpecLoader specLoader) {
        List<PlayerActionSpec> specs = specLoader.loadAll();
        this.allActions = specs.stream()
                .map(spec -> (GameAction) new DataDrivenAction(spec))
                .toList();
        this.actionsByCode = allActions.stream()
                .collect(Collectors.toMap(
                        a -> a.type().code(),
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    @Override
    public GameAction getAction(String actionCode) {
        GameAction action = actionsByCode.get(actionCode);
        if (action == null) {
            throw new InvalidActionException("Unknown action code: " + actionCode);
        }
        return action;
    }

    @Override
    public Collection<GameAction> allActions() {
        return Collections.unmodifiableList(allActions);
    }
}
