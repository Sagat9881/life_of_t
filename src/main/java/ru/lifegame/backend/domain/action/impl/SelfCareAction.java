package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

public class SelfCareAction implements GameAction {

    @Override
    public ActionType type() { return Actions.SELF_CARE; }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) {
        return GameBalance.SELF_CARE_TIME_COST;
    }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        int timeCost = calculateTimeCost(session);
        StatChanges changes = new StatChanges(
                GameBalance.SELF_CARE_ENERGY, 0, GameBalance.SELF_CARE_STRESS,
                GameBalance.SELF_CARE_MOOD, 0, GameBalance.SELF_CARE_SELF_ESTEEM
        );
        return new ActionResult(
                type(), timeCost,
                "Татьяна уделила время себе. Стресс отступил.",
                changes,
                Map.of(),
                Map.of(),
                false, false, false, false
        );
    }
}
