package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

public class JoggingAction implements GameAction {

    @Override
    public ActionType type() { return StandardActionType.JOGGING; }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) {
        return GameBalance.timeHours(1);
    }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        int timeCost = calculateTimeCost(session);
        StatChanges changes = new StatChanges(
                -20, 15, -10,
                0, 0, 10
        );
        return new ActionResult(
                type(), timeCost,
                "Татьяна совершила пробежку. Здоровый образ жизни!",
                changes,
                Map.of(),
                Map.of(),
                false, false, false, false
        );
    }
}
