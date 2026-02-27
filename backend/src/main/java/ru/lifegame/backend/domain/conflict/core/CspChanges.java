package ru.lifegame.backend.domain.conflict.core;

public record CspChanges(int player, int opponent) {
    public static CspChanges none() {
        return new CspChanges(0, 0);
    }
}
