package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.StatChanges;

import java.util.Map;

public class WalkDogAction implements GameAction {

    @Override
    public ActionType type() { return Actions.WALK_DOG; }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) {
        int base = GameBalance.WALK_DOG_TIME_COST;
        int dogCare = session.player().skills().getLevel("dog_care");
        return Math.max(base - dogCare / 50, 1);
    }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        int timeCost = calculateTimeCost(session);
        StatChanges changes = new StatChanges(
                GameBalance.WALK_DOG_ENERGY, GameBalance.WALK_DOG_HEALTH,
                GameBalance.WALK_DOG_STRESS, GameBalance.WALK_DOG_MOOD, 0, 0
        );
        return new ActionResult(
                type(), timeCost,
                "Татьяна погуляла с Сэмом. Свежий воздух пошёл на пользу.",
                changes,
                Map.of(),
                Map.of("SAM", GameBalance.WALK_DOG_PET_MOOD),
                false, false, false, false
        );
    }
}
