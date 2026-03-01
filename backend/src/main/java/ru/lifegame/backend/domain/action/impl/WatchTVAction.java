package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

public class WatchTVAction implements GameAction {

    @Override
    public ActionType type() { return StandardActionType.WATCH_TV; }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) {
        return GameBalance.timeHours(1);
    }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        int timeCost = calculateTimeCost(session);
        StatChanges changes = new StatChanges(
                0, 0, -5,
                15, 0, 0
        );
        return new ActionResult(
                type(), timeCost,
                "Татьяна посмотрела сериал. Расслабляющий вечер.",
                changes,
                Map.of(),
                Map.of(),
                false, false, false, false
        );
    }
}
