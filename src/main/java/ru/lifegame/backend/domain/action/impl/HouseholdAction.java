package ru.lifegame.backend.domain.action.impl;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.relationship.NpcCode;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;

/**
 * Home chores action - cleaning, cooking, organizing.
 * Costs energy and mood but maintains home environment and husband relationship.
 */
public class HouseholdAction implements GameAction {
    @Override
    public ActionType type() {
        return StandardActionType.HOUSEHOLD;
    }

    @Override
    public int calculateTimeCost(GameSessionReadModel state) {
        return GameBalance.HOUSEHOLD_TIME_COST;
    }

    @Override
    public ActionResult calculate(GameSessionReadModel state) {
        StatChanges stats = new StatChanges(
                GameBalance.HOUSEHOLD_ENERGY,  // costs energy
                0,  // no health change
                0,  // no stress change
                GameBalance.HOUSEHOLD_MOOD,    // slightly decreases mood
                0,  // no money
                0   // no self-esteem
        );

        Map<String, Integer> relationshipChanges = Map.of(
                NpcCode.HUSBAND.name(), 5  // husband appreciates household help
        );

        return new ActionResult(
                StandardActionType.HOUSEHOLD,
                calculateTimeCost(state),
                "Домашние дела сделаны. Муж ценит уют, но вы немного устали.",
                stats,
                relationshipChanges,
                Map.of(),  // no pet changes
                false,     // not rested
                false,     // not worked
                true,      // interacted with husband (indirectly)
                false      // no father interaction
        );
    }
}
