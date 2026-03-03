package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

public class WalkDogParkAction implements GameAction {

    private static final int TIME_COST = 1;

    @Override
    public ActionType type() { return StandardActionType.WALK_DOG_PARK; }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) { return TIME_COST; }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        StatChanges changes = new StatChanges(-10, 0, -10, 10, 0, 0);
        return new ActionResult(
                type(), TIME_COST,
                "Татьяна погуляла с Сэмом в парке. Собака счастлива!",
                changes,
                Map.of(),
                Map.of("SAM", 20),
                false, false, false, false
        );
    }
}
