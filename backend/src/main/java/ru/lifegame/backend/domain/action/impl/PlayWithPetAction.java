package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

public class PlayWithPetAction implements GameAction {

    private static final int TIME_COST = 1;

    @Override
    public ActionType type() { return StandardActionType.PLAY_WITH_PET; }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) { return TIME_COST; }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        StatChanges changes = new StatChanges(-5, 0, -10, 10, 0, 0);
        return new ActionResult(
                type(), TIME_COST,
                "Татьяна поиграла с Барсиком. Кот доволен!",
                changes,
                Map.of(),
                Map.of("BARSIK", 15),
                false, false, false, false
        );
    }
}
