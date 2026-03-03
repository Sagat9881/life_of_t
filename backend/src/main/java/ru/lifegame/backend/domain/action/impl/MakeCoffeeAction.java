package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

public class MakeCoffeeAction implements GameAction {

    private static final int TIME_COST = 1;

    @Override
    public ActionType type() { return StandardActionType.MAKE_COFFEE; }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) { return TIME_COST; }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        StatChanges changes = new StatChanges(10, 0, 0, 5, 0, 0);
        return new ActionResult(
                type(), TIME_COST,
                "Татьяна сделала кофе. Бодрящий перерыв.",
                changes,
                Map.of(),
                Map.of(),
                false, false, false, false
        );
    }
}
