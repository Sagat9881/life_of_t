package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

public class TalkToColleagueAction implements GameAction {

    private static final int TIME_COST = 1; // 0.5 hours = 30 minutes

    @Override
    public ActionType type() { return StandardActionType.TALK_TO_COLLEAGUE; }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) {
        return TIME_COST;
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
