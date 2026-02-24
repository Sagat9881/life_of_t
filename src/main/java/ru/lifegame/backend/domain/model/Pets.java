package ru.lifegame.backend.domain.model;

import ru.lifegame.backend.domain.balance.GameBalance;

import java.util.*;

public class Pets {
    private final Map<PetCode, Pet> map;

    public Pets(Map<PetCode, Pet> map) {
        this.map = new EnumMap<>(map);
    }

    public Pet get(PetCode code) {
        return map.get(code);
    }

    public void applyMoodChange(PetCode code, int delta) {
        Pet p = map.get(code);
        if (p != null) {
            map.put(code, p.changeMood(delta));
        }
    }

    public void applyAttentionChange(PetCode code, int delta) {
        Pet p = map.get(code);
        if (p != null) {
            map.put(code, p.changeAttention(delta));
        }
    }

    public void applyDailyDecay() {
        for (PetCode code : map.keySet()) {
            map.put(code, map.get(code).applyDailyDecay());
        }
    }

    public boolean hasDeadPet() {
        return map.values().stream().anyMatch(Pet::isDead);
    }

    public Map<PetCode, Pet> all() {
        return Collections.unmodifiableMap(map);
    }

    public static Pets initial() {
        var m = new EnumMap<PetCode, Pet>(PetCode.class);
        m.put(PetCode.GARFIELD, new Pet(PetCode.GARFIELD, "Гарфилд",
                GameBalance.PET_INITIAL_SATIETY, GameBalance.PET_INITIAL_ATTENTION,
                GameBalance.PET_INITIAL_HEALTH, GameBalance.PET_INITIAL_MOOD));
        m.put(PetCode.SAM, new Pet(PetCode.SAM, "Сэм",
                GameBalance.PET_SAM_INITIAL_SATIETY, GameBalance.PET_SAM_INITIAL_ATTENTION,
                GameBalance.PET_SAM_INITIAL_HEALTH, GameBalance.PET_SAM_INITIAL_MOOD));
        return new Pets(m);
    }
}
