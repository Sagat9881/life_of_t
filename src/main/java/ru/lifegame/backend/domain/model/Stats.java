package ru.lifegame.backend.domain.model;

import ru.lifegame.backend.domain.balance.GameBalance;

public record Stats(
        int energy,
        int health,
        int stress,
        int mood,
        int money,
        int selfEsteem
) {
    public Stats {
        energy = clamp(energy);
        health = clamp(health);
        stress = clamp(stress);
        mood = clamp(mood);
        selfEsteem = clamp(selfEsteem);
    }

    public Stats apply(StatChanges c) {
        return new Stats(
                energy + c.energy(),
                health + c.health(),
                stress + c.stress(),
                mood + c.mood(),
                money + c.money(),
                selfEsteem + c.selfEsteem()
        );
    }

    public Stats changeEnergy(int delta) {
        return new Stats(energy + delta, health, stress, mood, money, selfEsteem);
    }

    public Stats changeStress(int delta) {
        return new Stats(energy, health, stress + delta, mood, money, selfEsteem);
    }

    public Stats changeMood(int delta) {
        return new Stats(energy, health, stress, mood + delta, money, selfEsteem);
    }

    public Stats changeMoney(int delta) {
        return new Stats(energy, health, stress, mood, money + delta, selfEsteem);
    }

    private static int clamp(int value) {
        return Math.max(GameBalance.STAT_MIN, Math.min(GameBalance.STAT_MAX, value));
    }

    public static Stats initial() {
        return new Stats(
                GameBalance.INITIAL_ENERGY,
                GameBalance.INITIAL_HEALTH,
                GameBalance.INITIAL_STRESS,
                GameBalance.INITIAL_MOOD,
                GameBalance.INITIAL_MONEY,
                GameBalance.INITIAL_SELF_ESTEEM
        );
    }
}
