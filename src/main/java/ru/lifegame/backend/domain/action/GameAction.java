package ru.lifegame.backend.domain.action;

public interface GameAction {
    ActionType type();
    int calculateTimeCost(GameSessionReadModel session);
    ActionResult calculate(GameSessionReadModel session);
}
