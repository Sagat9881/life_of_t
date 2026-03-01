package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

public class WalkDogParkAction implements GameAction {

    @Override
    public ActionType type() { return StandardActionType.WALK_DOG_PARK; }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) {
        return GameBalance.timeHours(1);
    }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        int timeCost = calculateTimeCost(session);
        StatChanges changes = new StatChanges(
                -10, 0, -10,
                10, 0, 0
        );
        return new ActionResult(
                type(), timeCost,
                "Татьяна погуляла с Сэмом. Собака счастлива!",
                changes,
                Map.of(),
                Map.of("sam", 20),
                false, false, false, false
        );
    }
}
