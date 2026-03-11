package ru.lifegame.backend.domain.model.pet;

import ru.lifegame.backend.domain.balance.GameBalance;

public record Pet(
        String id,
        String name,
        String type,
        int satiety,
        int attention,
        int health,
        int mood
) {
    public Pet {
        satiety   = clamp(satiety);
        attention = clamp(attention);
        health    = clamp(health);
        mood      = clamp(mood);
    }

    /**
     * Applies end-of-day decay per game-balance.yml (pets.decay).
     *
     * 1. Satiety   − PET_DAILY_SATIETY_DECAY   (floored at STAT_MIN)
     * 2. Attention − PET_DAILY_ATTENTION_DECAY  (floored at STAT_MIN)
     * 3. If satiety drops below PET_LOW_SATIETY_THRESHOLD:
     *      health − PET_HEALTH_DECAY_LOW_SATIETY
     *      mood   − PET_MOOD_DECAY_LOW_SATIETY
     */
    public Pet applyDailyDecay() {
        int newSatiety   = Math.max(GameBalance.STAT_MIN, satiety   - GameBalance.PET_DAILY_SATIETY_DECAY);
        int newAttention = Math.max(GameBalance.STAT_MIN, attention - GameBalance.PET_DAILY_ATTENTION_DECAY);
        int newHealth = health;
        int newMood   = mood;
        if (newSatiety < GameBalance.PET_LOW_SATIETY_THRESHOLD) {
            newHealth = Math.max(GameBalance.STAT_MIN, newHealth - GameBalance.PET_HEALTH_DECAY_LOW_SATIETY);
            newMood   = Math.max(GameBalance.STAT_MIN, newMood   - GameBalance.PET_MOOD_DECAY_LOW_SATIETY);
        }
        return new Pet(id, name, type, newSatiety, newAttention, newHealth, newMood);
    }

    public Pet withSatiety(int newSatiety)       { return new Pet(id, name, type, newSatiety, attention, health, mood); }
    public Pet withAttention(int newAttention)   { return new Pet(id, name, type, satiety, newAttention, health, mood); }
    public Pet withHealth(int newHealth)         { return new Pet(id, name, type, satiety, attention, newHealth, mood); }
    public Pet withMood(int newMood)             { return new Pet(id, name, type, satiety, attention, health, newMood); }

    /** Clamp for pet stats bounded by [STAT_MIN, STAT_MAX] per game-balance.yml. */
    private static int clamp(int value) {
        return Math.max(GameBalance.STAT_MIN, Math.min(GameBalance.STAT_MAX, value));
    }
}
