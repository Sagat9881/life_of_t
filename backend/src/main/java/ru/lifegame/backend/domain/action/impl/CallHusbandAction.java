package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

public class CallHusbandAction implements GameAction {

    private static final int TIME_COST = 1;

    @Override
    public ActionType type() { return StandardActionType.CALL_HUSBAND; }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) { return TIME_COST; }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        StatChanges changes = new StatChanges(0, 0, 0, 10, 0, 0);
        return new ActionResult(
                type(), TIME_COST,
                "Татьяна позвонила Александру. Приятный разговор поднял настроение.",
                changes,
                Map.of("HUSBAND", 5),
                Map.of(),
                false, false, true, false
        );
    }
}
