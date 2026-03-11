package ru.lifegame.backend.domain.model.pet;

import ru.lifegame.backend.domain.balance.GameBalance;

import java.util.HashMap;
import java.util.Map;

public class Pets {

    private final Map<String, Pet> pets;

    public Pets(Map<String, Pet> pets) {
        this.pets = new HashMap<>(pets);
    }

    public Pet get(String petId) {
        return pets.get(petId);
    }

    public void applyDailyDecay() {
        for (Map.Entry<String, Pet> entry : new HashMap<>(pets).entrySet()) {
            pets.put(entry.getKey(), entry.getValue().applyDailyDecay());
        }
    }

    public boolean hasDeadPet() {
        return pets.values().stream().anyMatch(pet -> pet.health() <= GameBalance.STAT_MIN);
    }

    public void applyMoodChange(String petId, int moodDelta) {
        Pet pet = pets.get(petId);
        if (pet != null) {
            pets.put(petId, pet.withMood(pet.mood() + moodDelta));
        }
    }

    public void applyAttentionChange(String petId, int attentionDelta) {
        Pet pet = pets.get(petId);
        if (pet != null) {
            pets.put(petId, pet.withAttention(pet.attention() + attentionDelta));
        }
    }

    public void applySatietyChange(String petId, int delta) {
        Pet pet = pets.get(petId);
        if (pet != null) {
            pets.put(petId, pet.withSatiety(pet.satiety() + delta));
        }
    }

    public Map<String, Pet> all() {
        return Map.copyOf(pets);
    }

    public static Pets initial() {
        return new Pets(Map.of(
                "barsik", new Pet(
                        "barsik", "\u0411\u0430\u0440\u0441\u0438\u043a", "CAT",
                        GameBalance.PET_INITIAL_SATIETY,
                        GameBalance.PET_INITIAL_ATTENTION,
                        GameBalance.PET_INITIAL_HEALTH,
                        GameBalance.PET_INITIAL_MOOD
                ),
                "sam", new Pet(
                        "sam", "\u0421\u044d\u043c", "DOG",
                        GameBalance.PET_SAM_INITIAL_SATIETY,
                        GameBalance.PET_SAM_INITIAL_ATTENTION,
                        GameBalance.PET_SAM_INITIAL_HEALTH,
                        GameBalance.PET_SAM_INITIAL_MOOD
                )
        ));
    }
}
