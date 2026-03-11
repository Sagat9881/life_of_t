package ru.lifegame.backend.domain.model.character;

import ru.lifegame.backend.domain.balance.GameBalance;

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
        // skill levels share the same ceiling as STAT_MAX (0..100) per game-balance.yml
        copy.put(skill, Math.min(current + delta, GameBalance.STAT_MAX));
        return new Skills(copy);
    }

    /** Convert skills to a plain map for serialization / view mapping. */
    public Map<String, Integer> toMap() {
        return values;
    }

    public static Skills initial() {
        return new Skills(Map.of(
                "cooking",       30,
                "dog_care",      40,
                "efficiency",    20,
                "empathy",       25,
                "humor",         15,
                "rhetoric",      10,
                "charisma",      20,
                "assertiveness", 15,
                "communication", 30
        ));
    }
}
