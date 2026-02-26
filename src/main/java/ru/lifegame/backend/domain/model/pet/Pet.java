package ru.lifegame.backend.domain.model.pet;

import ru.lifegame.backend.domain.balance.GameBalance;

public record Pet(
        PetCode code,
        String name,
        int satiety,
        int attention,
        int health,
        int mood
) {
    public Pet {
        satiety = clamp(satiety);
        attention = clamp(attention);
        health = clamp(health);
        mood = clamp(mood);
    }

    public Pet applyMoodChange(int delta) {
        return new Pet(code, name, satiety, attention, health, mood + delta);
    }

    public Pet applyAttentionChange(int delta) {
        return new Pet(code, name, satiety, attention + delta, health, mood);
    }

    public Pet applyDailyDecay() {
        int newSatiety = satiety - GameBalance.PET_DAILY_SATIETY_DECAY;
        int newAttention = attention - GameBalance.PET_DAILY_ATTENTION_DECAY;
        int newHealth = health;
        int newMood = mood;
        if (newSatiety < GameBalance.PET_LOW_SATIETY_THRESHOLD) {
            newHealth = health - GameBalance.PET_HEALTH_DECAY_LOW_SATIETY;
            newMood = mood - GameBalance.PET_MOOD_DECAY_LOW_SATIETY;
        }
        return new Pet(code, name, newSatiety, newAttention, newHealth, newMood);
    }

    private static int clamp(int value) {
        return Math.max(GameBalance.STAT_MIN, Math.min(GameBalance.STAT_MAX, value));
    }
}
