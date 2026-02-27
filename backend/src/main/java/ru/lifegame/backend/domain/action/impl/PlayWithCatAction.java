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
                "Татьяна поиграла с Гарфилдом. Кот доволен.",
                changes,
                Map.of(),
                Map.of("GARFIELD", GameBalance.PLAY_CAT_PET_MOOD),
                false, false, false, false
        );
    }
}
