package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.StatChanges;

import java.util.Map;

public class GoToWorkAction implements GameAction {

    @Override
    public ActionType type() { return Actions.GO_TO_WORK; }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) {
        int base = GameBalance.WORK_TIME_COST;
        int efficiency = session.player().skills().getLevel("efficiency");
        return Math.max(base - efficiency / 50, 6);
    }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        int timeCost = calculateTimeCost(session);
        StatChanges changes = new StatChanges(
                GameBalance.WORK_ENERGY, 0, GameBalance.WORK_STRESS,
                GameBalance.WORK_MOOD, GameBalance.WORK_MONEY, 0
        );
        return new ActionResult(
                type(), timeCost,
                "Татьяна поработала " + timeCost + " часов. Устала, но заработала деньги.",
                changes,
                Map.of("HUSBAND", -5, "FATHER", -2),
                Map.of(),
                false, true, false, false
        );
    }
}
