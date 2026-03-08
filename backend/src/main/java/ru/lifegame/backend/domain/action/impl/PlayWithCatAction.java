package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

public class PlayWithCatAction implements GameAction {

    @Override
    public ActionType type() { return Actions.PLAY_WITH_CAT; }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) {
        return GameBalance.PLAY_CAT_TIME_COST;
    }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        int timeCost = calculateTimeCost(session);
        StatChanges changes = new StatChanges(
                GameBalance.PLAY_CAT_ENERGY, 0, GameBalance.PLAY_CAT_STRESS,
                GameBalance.PLAY_CAT_MOOD, 0, 0
        );
        return new ActionResult(
                type(), timeCost,
                "\u0422\u0430\u0442\u044c\u044f\u043d\u0430 \u043f\u043e\u0438\u0433\u0440\u0430\u043b\u0430 \u0441 \u0411\u0430\u0440\u0441\u0438\u043a\u043e\u043c. \u041a\u043e\u0442 \u0434\u043e\u0432\u043e\u043b\u0435\u043d.",
                changes,
                Map.of(),
                Map.of("BARSIK", GameBalance.PLAY_CAT_PET_MOOD),
                false, false, false, false
        );
    }
}
