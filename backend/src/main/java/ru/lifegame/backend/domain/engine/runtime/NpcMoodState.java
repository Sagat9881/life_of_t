package ru.lifegame.backend.domain.engine.runtime;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable 6-axis mood state for a live NPC instance.
 * Axes: happiness, anxiety, loneliness, irritability, energy, affection.
 * Named NPCs use all 6; filler NPCs use happiness + energy only.
 */
public class NpcMoodState {
    private int happiness;
    private int anxiety;
    private int loneliness;
    private int irritability;
    private int energy;
    private int affection;
    private final List<MoodModifier> activeModifiers = new ArrayList<>();

    public record MoodModifier(String axis, int delta, int remainingDays) {}

    public static NpcMoodState fromTraits(java.util.Map<String, Integer> traits) {
        NpcMoodState m = new NpcMoodState();
        m.happiness = 50;
        m.anxiety = traits.getOrDefault("anxiety", 30);
        m.loneliness = 20;
        m.irritability = 0;
        m.energy = traits.getOrDefault("energy", 60);
        m.affection = 50;
        return m;
    }

    public static NpcMoodState fillerDefaults() {
        NpcMoodState m = new NpcMoodState();
        m.happiness = 50;
        m.energy = 50;
        return m;
    }

    public void dailyTick() {
        List<MoodModifier> expired = new ArrayList<>();
        for (MoodModifier mod : activeModifiers) {
            applyAxis(mod.axis(), mod.delta());
            if (mod.remainingDays() <= 1) expired.add(mod);
        }
        activeModifiers.removeAll(expired);
        activeModifiers.replaceAll(m -> new MoodModifier(m.axis(), m.delta(), m.remainingDays() - 1));

        loneliness = Math.min(100, loneliness + 5);
        energy = Math.min(100, energy + 10);
    }

    public void addModifier(String axis, int delta, int days) {
        activeModifiers.add(new MoodModifier(axis, delta, days));
    }

    public void onPlayerInteraction() {
        loneliness = Math.max(0, loneliness - 20);
        happiness = Math.min(100, happiness + 5);
        affection = Math.min(100, affection + 3);
    }

    private void applyAxis(String axis, int delta) {
        switch (axis) {
            case "happiness" -> happiness = clamp(happiness + delta);
            case "anxiety" -> anxiety = clamp(anxiety + delta);
            case "loneliness" -> loneliness = clamp(loneliness + delta);
            case "irritability" -> irritability = clamp(irritability + delta);
            case "energy" -> energy = clamp(energy + delta);
            case "affection" -> affection = clamp(affection + delta);
        }
    }

    private int clamp(int v) { return Math.max(0, Math.min(100, v)); }

    public int happiness() { return happiness; }
    public int anxiety() { return anxiety; }
    public int loneliness() { return loneliness; }
    public int irritability() { return irritability; }
    public int energy() { return energy; }
    public int affection() { return affection; }

    public double urgencyScore() {
        return (loneliness * 0.3 + irritability * 0.25 + anxiety * 0.2
                + (100 - happiness) * 0.15 + (100 - energy) * 0.1) / 100.0;
    }
}
