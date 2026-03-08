package ru.lifegame.backend.domain.model.pet;

import ru.lifegame.backend.domain.balance.GameBalance;

public record Pet(
    String id,
    String name,
    PetType type,
    int satiety,
    int attention,
    int health,
    int mood
) {
    public Pet {
        satiety = Math.max(0, Math.min(100, satiety));
        attention = Math.max(0, Math.min(100, attention));
        health = Math.max(0, Math.min(100, health));
        mood = Math.max(0, Math.min(100, mood));
    }

    public Pet applyDailyDecay() {
        int newSatiety = Math.max(0, satiety - GameBalance.PET_DAILY_SATIETY_DECAY);
        int newAttention = Math.max(0, attention - GameBalance.PET_DAILY_ATTENTION_DECAY);
        int newHealth = health;
        int newMood = mood;
        if (newSatiety < GameBalance.PET_LOW_SATIETY_THRESHOLD) {
            newHealth = Math.max(0, newHealth - GameBalance.PET_HEALTH_DECAY_LOW_SATIETY);
            newMood = Math.max(0, newMood - GameBalance.PET_MOOD_DECAY_LOW_SATIETY);
        }
        return new Pet(id, name, type, newSatiety, newAttention, newHealth, newMood);
    }

    public Pet withSatiety(int newSatiety) {
        return new Pet(id, name, type, newSatiety, attention, health, mood);
    }

    public Pet withAttention(int newAttention) {
        return new Pet(id, name, type, satiety, newAttention, health, mood);
    }

    public Pet withHealth(int newHealth) {
        return new Pet(id, name, type, satiety, attention, newHealth, mood);
    }

    public Pet withMood(int newMood) {
        return new Pet(id, name, type, satiety, attention, health, newMood);
    }
}
