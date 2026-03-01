package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

public class RestOnBenchAction implements GameAction {

    @Override
    public ActionType type() { return StandardActionType.REST_ON_BENCH; }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) {
        return GameBalance.timeHours(1);
    }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        int timeCost = calculateTimeCost(session);
        StatChanges changes = new StatChanges(
                15, 0, -15,
                10, 0, 0
        );
        return new ActionResult(
                type(), timeCost,
                "Татьяна отдохнула на скамейке. Свежий воздух освежает.",
                changes,
                Map.of(),
                Map.of(),
                true, false, false, false
        );
    }
}
