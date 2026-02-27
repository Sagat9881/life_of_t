package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

public class RestAtHomeAction implements GameAction {

    @Override
    public ActionType type() { return Actions.REST_AT_HOME; }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) {
        return GameBalance.REST_TIME_COST;
    }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        int timeCost = calculateTimeCost(session);
        StatChanges changes = new StatChanges(
                GameBalance.REST_ENERGY, 0, GameBalance.REST_STRESS,
                GameBalance.REST_MOOD, 0, 0
        );
        return new ActionResult(
                type(), timeCost,
                "Татьяна отдохнула дома. Силы восстановлены.",
                changes,
                Map.of(),
                Map.of(),
                true, false, false, false
        );
    }
}
