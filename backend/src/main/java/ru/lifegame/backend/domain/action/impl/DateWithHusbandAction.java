package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

public class DateWithHusbandAction implements GameAction {

    @Override
    public ActionType type() { return Actions.DATE_WITH_HUSBAND; }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) {
        return GameBalance.DATE_HUSBAND_TIME_COST;
    }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        int timeCost = calculateTimeCost(session);
        StatChanges changes = new StatChanges(
                GameBalance.DATE_ENERGY, 0, GameBalance.DATE_STRESS,
                GameBalance.DATE_MOOD, 0, 0
        );
        return new ActionResult(
                type(), timeCost,
                "Татьяна провела романтический вечер с мужем.",
                changes,
                Map.of("HUSBAND", GameBalance.DATE_CLOSENESS),
                Map.of(),
                false, false, true, false
        );
    }
}
