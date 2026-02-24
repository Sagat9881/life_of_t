package ru.lifegame.backend.domain.conflict;

import ru.lifegame.backend.domain.balance.GameBalance;

public record ConflictStressPoints(int player, int opponent) {

    public ConflictStressPoints {
        player = Math.max(0, player);
        opponent = Math.max(0, opponent);
    }

    public static ConflictStressPoints initial() {
        return new ConflictStressPoints(GameBalance.CSP_BASE, GameBalance.CSP_BASE);
    }

    public ConflictStressPoints apply(CspChanges c) {
        return new ConflictStressPoints(player + c.playerDelta(), opponent + c.opponentDelta());
    }

    public boolean isPlayerDefeated() {
        return player <= 0;
    }

    public boolean isOpponentDefeated() {
        return opponent <= 0;
    }
}
