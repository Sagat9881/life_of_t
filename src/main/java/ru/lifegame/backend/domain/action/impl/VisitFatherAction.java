package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.StatChanges;

import java.util.Map;

public class VisitFatherAction implements GameAction {

    @Override
    public ActionType type() { return Actions.VISIT_FATHER; }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) {
        return GameBalance.VISIT_FATHER_TIME_COST;
    }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        int timeCost = calculateTimeCost(session);
        StatChanges changes = new StatChanges(
                GameBalance.VISIT_FATHER_ENERGY, 0, 0,
                GameBalance.VISIT_FATHER_MOOD, 0, 0
        );
        return new ActionResult(
                type(), timeCost,
                "Татьяна навестила отца. Он рад визиту.",
                changes,
                Map.of("FATHER", GameBalance.VISIT_FATHER_CLOSENESS),
                Map.of(),
                false, false, false, true
        );
    }
}
