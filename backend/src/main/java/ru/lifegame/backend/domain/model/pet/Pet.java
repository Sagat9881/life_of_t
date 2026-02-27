package ru.lifegame.backend.domain.model.pet;

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

    public void applyDailyDecay() {
        // Decay handled through withXxx methods
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
