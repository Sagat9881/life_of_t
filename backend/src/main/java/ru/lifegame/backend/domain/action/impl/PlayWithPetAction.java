package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

public class PlayWithPetAction implements GameAction {

    @Override
    public ActionType type() { return StandardActionType.PLAY_WITH_PET; }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) {
        return GameBalance.timeHours(1);
    }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        int timeCost = calculateTimeCost(session);
        StatChanges changes = new StatChanges(
                -5, 0, -10,
                10, 0, 0
        );
        return new ActionResult(
                type(), timeCost,
                "Татьяна поиграла с Гарфилдом. Кот доволен!",
                changes,
                Map.of(),
                Map.of("garfield", 15),
                false, false, false, false
        );
    }
}
