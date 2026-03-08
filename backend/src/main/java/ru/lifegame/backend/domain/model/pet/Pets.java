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
        return pets.values().stream().anyMatch(pet -> pet.health() <= 0);
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
                "barsik", "\u0411\u0430\u0440\u0441\u0438\u043a", PetType.CAT,
                GameBalance.PET_INITIAL_SATIETY, GameBalance.PET_INITIAL_ATTENTION,
                GameBalance.PET_INITIAL_HEALTH, GameBalance.PET_INITIAL_MOOD
            ),
            "sam", new Pet(
                "sam", "\u0421\u044d\u043c", PetType.DOG,
                GameBalance.PET_SAM_INITIAL_SATIETY, GameBalance.PET_SAM_INITIAL_ATTENTION,
                GameBalance.PET_SAM_INITIAL_HEALTH, GameBalance.PET_SAM_INITIAL_MOOD
            )
        ));
    }
}
