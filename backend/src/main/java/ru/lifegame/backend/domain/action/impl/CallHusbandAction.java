package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

public class CallHusbandAction implements GameAction {

    @Override
    public ActionType type() { return StandardActionType.CALL_HUSBAND; }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) {
        return GameBalance.timeHours(0.5);
    }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        int timeCost = calculateTimeCost(session);
        StatChanges changes = new StatChanges(
                0, 0, 0,
                10, 0, 0
        );
        return new ActionResult(
                type(), timeCost,
                "Татьяна позвонила Александру. Приятный разговор поднял настроение.",
                changes,
                Map.of("husband", 5),
                Map.of(),
                false, false, true, false
        );
    }
}
