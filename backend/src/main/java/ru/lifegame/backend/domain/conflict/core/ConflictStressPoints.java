package ru.lifegame.backend.domain.conflict.core;

import ru.lifegame.backend.domain.balance.GameBalance;

public record ConflictStressPoints(int player, int opponent) {
    public static ConflictStressPoints initial() {
        return new ConflictStressPoints(GameBalance.INITIAL_CSP, GameBalance.INITIAL_CSP);
    }

    public ConflictStressPoints apply(CspChanges changes) {
        return new ConflictStressPoints(
                Math.max(0, player + changes.player()),
                Math.max(0, opponent + changes.opponent())
        );
    }

    public boolean isPlayerDefeated() { return player == 0; }
    public boolean isOpponentDefeated() { return opponent == 0; }
}
