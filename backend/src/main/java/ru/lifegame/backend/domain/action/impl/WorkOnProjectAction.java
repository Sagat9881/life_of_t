package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

public class WorkOnProjectAction implements GameAction {

    private static final int TIME_COST = 3; // 3 hours

    @Override
    public ActionType type() { return StandardActionType.WORK_ON_PROJECT; }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) {
        return TIME_COST;
    }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        int timeCost = calculateTimeCost(session);
        StatChanges changes = new StatChanges(
                -30, 0, 10,
                0, 800, 0
        );
        return new ActionResult(
                type(), timeCost,
                "Татьяна поработала над проектом. +800₽ заработано.",
                changes,
                Map.of(),
                Map.of(),
                false, true, false, false
        );
    }
}
