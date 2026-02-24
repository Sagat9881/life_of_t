package ru.lifegame.backend.domain.model;

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

    public StatChanges combine(StatChanges other) {
        return new StatChanges(
                energy + other.energy(),
                health + other.health(),
                stress + other.stress(),
                mood + other.mood(),
                money + other.money(),
                selfEsteem + other.selfEsteem()
        );
    }
}
