package ru.lifegame.backend.domain.conflict;

public record CspChanges(int playerDelta, int opponentDelta) {
    public static CspChanges none() {
        return new CspChanges(0, 0);
    }
}
