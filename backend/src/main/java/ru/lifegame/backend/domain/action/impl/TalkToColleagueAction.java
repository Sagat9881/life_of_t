package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

public class TalkToColleagueAction implements GameAction {

    @Override
    public ActionType type() { return StandardActionType.TALK_TO_COLLEAGUE; }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) {
        return GameBalance.timeHours(0.5);
    }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        int timeCost = calculateTimeCost(session);
        StatChanges changes = new StatChanges(
                0, 0, -5,
                10, 0, 0
        );
        return new ActionResult(
                type(), timeCost,
                "Татьяна поболтала с коллегой. Приятное общение.",
                changes,
                Map.of(),
                Map.of(),
                false, false, false, false
        );
    }
}
