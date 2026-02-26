package ru.lifegame.backend.domain.model.character;

import java.util.HashMap;
import java.util.Map;

public record Skills(Map<String, Integer> values) {
    public Skills {
        values = Map.copyOf(values);
    }

    public boolean hasLevel(String skill, int required) {
        return getLevel(skill) >= required;
    }

    public int getLevel(String skill) {
        return values.getOrDefault(skill, 0);
    }

    public Skills improve(String skill, int delta) {
        var copy = new HashMap<>(values);
        int current = getLevel(skill);
        copy.put(skill, Math.min(current + delta, 100));
        return new Skills(copy);
    }

    public static Skills initial() {
        return new Skills(Map.of(
                "cooking", 30,
                "dog_care", 40,
                "efficiency", 20,
                "empathy", 25,
                "humor", 15,
                "rhetoric", 10,
                "charisma", 20,
                "assertiveness", 15,
                "communication", 30
        ));
    }
}
