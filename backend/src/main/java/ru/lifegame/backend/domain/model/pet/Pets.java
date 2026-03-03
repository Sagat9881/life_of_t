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
        for (String key : pets.keySet()) {
            Pet p = pets.get(key);
            int newSatiety   = p.satiety()   - GameBalance.PET_DAILY_SATIETY_DECAY;
            int newAttention = p.attention()  - GameBalance.PET_DAILY_ATTENTION_DECAY;
            int newHealth    = p.health();
            int newMood      = p.mood();
            if (newSatiety < GameBalance.PET_LOW_SATIETY_THRESHOLD) {
                newHealth -= GameBalance.PET_HEALTH_DECAY_LOW_SATIETY;
                newMood   -= GameBalance.PET_MOOD_DECAY_LOW_SATIETY;
            }
            pets.put(key, p.withSatiety(newSatiety).withAttention(newAttention)
                    .withHealth(newHealth).withMood(newMood));
        }
    }

    public boolean hasDeadPet() {
        return pets.values().stream().anyMatch(p -> p.health() <= 0);
    }

    public void applyMoodChange(PetCode petCode, int moodDelta) {
        Pet pet = pets.get(petCode.code());
        if (pet != null) {
            pets.put(petCode.code(), pet.withMood(pet.mood() + moodDelta));
        }
    }

    public void applyAttentionChange(PetCode petCode, int attentionDelta) {
        Pet pet = pets.get(petCode.code());
        if (pet != null) {
            pets.put(petCode.code(), pet.withAttention(pet.attention() + attentionDelta));
        }
    }

    public Map<String, Pet> all() {
        return Map.copyOf(pets);
    }

    public static Pets initial() {
        return new Pets(Map.of(
            "barsik", new Pet(
                "barsik", "Барсик", PetType.CAT,
                GameBalance.PET_INITIAL_SATIETY,
                GameBalance.PET_INITIAL_ATTENTION,
                GameBalance.PET_INITIAL_HEALTH,
                GameBalance.PET_INITIAL_MOOD
            ),
            "sam", new Pet(
                "sam", "Сэм", PetType.DOG,
                GameBalance.PET_SAM_INITIAL_SATIETY,
                GameBalance.PET_SAM_INITIAL_ATTENTION,
                GameBalance.PET_SAM_INITIAL_HEALTH,
                GameBalance.PET_SAM_INITIAL_MOOD
            )
        ));
    }
}
