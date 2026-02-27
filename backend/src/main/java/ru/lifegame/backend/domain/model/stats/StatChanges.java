package ru.lifegame.backend.domain.model.stats;

public record StatChanges(
        int energy,
        int health,
        int stress,
        int mood,
        int money,
        int selfEsteem
) {
    public static StatChanges none() {
        return new StatChanges(0, 0, 0, 0, 0, 0);
    }
}
