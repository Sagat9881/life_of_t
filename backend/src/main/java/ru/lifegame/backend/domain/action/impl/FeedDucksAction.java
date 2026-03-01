package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

public class FeedDucksAction implements GameAction {

    @Override
    public ActionType type() { return StandardActionType.FEED_DUCKS; }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) {
        return GameBalance.timeHours(0.5);
    }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        int timeCost = calculateTimeCost(session);
        StatChanges changes = new StatChanges(
                0, 0, -5,
                10, 0, 5
        );
        return new ActionResult(
                type(), timeCost,
                "Татьяна покормила уток. Милые птицы подняли настроение!",
                changes,
                Map.of(),
                Map.of(),
                false, false, false, false
        );
    }
}
