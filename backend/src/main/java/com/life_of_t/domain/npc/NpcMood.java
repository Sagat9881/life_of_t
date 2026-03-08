package com.life_of_t.domain.npc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 6-axis mood system for NPC. All values 0-100.
 * Axes: happiness, anxiety, loneliness, irritability, energy, affection.
 * Mood is modified by MoodModifiers with duration and decays daily.
 * Extreme values can override scheduled behavior.
 */
public class NpcMood {

    private int happiness;
    private int anxiety;
    private int loneliness;
    private int irritability;
    private int energy;
    private int affection;
    private final List<MoodModifier> activeModifiers;

    public record MoodModifier(
            String axis,
            int delta,
            int remainingDays,
            String source
    ) {
        public MoodModifier withDecrementedDuration() {
            return new MoodModifier(axis, delta, remainingDays - 1, source);
        }

        public boolean isExpired() {
            return remainingDays <= 0;
        }
    }

    public NpcMood(int happiness, int anxiety, int loneliness,
                   int irritability, int energy, int affection) {
        this.happiness = clamp(happiness);
        this.anxiety = clamp(anxiety);
        this.loneliness = clamp(loneliness);
        this.irritability = clamp(irritability);
        this.energy = clamp(energy);
        this.affection = clamp(affection);
        this.activeModifiers = new ArrayList<>();
    }

    /**
     * Create from XML initial values map.
     * Expected keys: happiness, anxiety, loneliness, irritability, energy, affection.
     * Missing keys default to 50.
     */
    public static NpcMood fromSpec(Map<String, Integer> initialValues) {
        return new NpcMood(
                initialValues.getOrDefault("happiness", 50),
                initialValues.getOrDefault("anxiety", 50),
                initialValues.getOrDefault("loneliness", 50),
                initialValues.getOrDefault("irritability", 50),
                initialValues.getOrDefault("energy", 50),
                initialValues.getOrDefault("affection", 50)
        );
    }

    /**
     * Simplified mood for filler NPCs — only happiness and energy are tracked.
     */
    public static NpcMood fillerMood(int happiness, int energy) {
        return new NpcMood(happiness, 0, 0, 0, energy, 0);
    }

    public void addModifier(MoodModifier modifier) {
        activeModifiers.add(modifier);
        applyDelta(modifier.axis(), modifier.delta());
    }

    /**
     * Called at end of each day. Decrements modifier durations,
     * removes expired ones, applies base decay toward neutral (50).
     */
    public void dailyTick() {
        Iterator<MoodModifier> it = activeModifiers.iterator();
        while (it.hasNext()) {
            MoodModifier mod = it.next();
            if (mod.isExpired()) {
                reverseDelta(mod.axis(), mod.delta());
                it.remove();
            } else {
                int idx = activeModifiers.indexOf(mod);
                activeModifiers.set(idx, mod.withDecrementedDuration());
            }
        }
        decayTowardNeutral();
    }

    /**
     * Natural decay: each axis moves 5 points toward 50 per day.
     */
    private void decayTowardNeutral() {
        this.happiness = decayAxis(happiness);
        this.anxiety = decayAxis(anxiety);
        this.loneliness = decayAxis(loneliness);
        this.irritability = decayAxis(irritability);
        this.energy = decayAxis(energy);
        this.affection = decayAxis(affection);
    }

    private int decayAxis(int current) {
        int neutral = 50;
        int decayRate = 5;
        if (current > neutral) {
            return Math.max(neutral, current - decayRate);
        } else if (current < neutral) {
            return Math.min(neutral, current + decayRate);
        }
        return current;
    }

    public int getAxis(String axisName) {
        return switch (axisName.toLowerCase()) {
            case "happiness" -> happiness;
            case "anxiety" -> anxiety;
            case "loneliness" -> loneliness;
            case "irritability" -> irritability;
            case "energy" -> energy;
            case "affection" -> affection;
            default -> throw new IllegalArgumentException("Unknown mood axis: " + axisName);
        };
    }

    public void setAxis(String axisName, int value) {
        switch (axisName.toLowerCase()) {
            case "happiness" -> this.happiness = clamp(value);
            case "anxiety" -> this.anxiety = clamp(value);
            case "loneliness" -> this.loneliness = clamp(value);
            case "irritability" -> this.irritability = clamp(value);
            case "energy" -> this.energy = clamp(value);
            case "affection" -> this.affection = clamp(value);
            default -> throw new IllegalArgumentException("Unknown mood axis: " + axisName);
        }
    }

    /**
     * Returns the most extreme axis (furthest from 50) — used for mood override checks.
     */
    public String dominantAxis() {
        int maxDev = 0;
        String dominant = "happiness";
        Map<String, Integer> axes = Map.of(
                "happiness", happiness, "anxiety", anxiety,
                "loneliness", loneliness, "irritability", irritability,
                "energy", energy, "affection", affection
        );
        for (var entry : axes.entrySet()) {
            int deviation = Math.abs(entry.getValue() - 50);
            if (deviation > maxDev) {
                maxDev = deviation;
                dominant = entry.getKey();
            }
        }
        return dominant;
    }

    /**
     * True if any axis exceeds extreme threshold (>80 or <20).
     * Used to trigger mood override of scheduled activity.
     */
    public boolean hasExtremeState() {
        return happiness < 20 || happiness > 80 ||
               anxiety > 80 || loneliness > 80 ||
               irritability > 80 || energy < 20 || affection < 20;
    }

    /**
     * Urgency score: higher means NPC needs attention more urgently.
     * Sum of deviations from neutral for negative axes.
     */
    public double urgencyScore() {
        return (Math.max(0, anxiety - 50) * 1.5) +
               (Math.max(0, loneliness - 50) * 1.2) +
               (Math.max(0, irritability - 50) * 1.3) +
               (Math.max(0, 50 - happiness) * 1.0) +
               (Math.max(0, 50 - energy) * 0.8);
    }

    private void applyDelta(String axis, int delta) {
        setAxis(axis, getAxis(axis) + delta);
    }

    private void reverseDelta(String axis, int delta) {
        setAxis(axis, getAxis(axis) - delta);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    // Getters
    public int happiness() { return happiness; }
    public int anxiety() { return anxiety; }
    public int loneliness() { return loneliness; }
    public int irritability() { return irritability; }
    public int energy() { return energy; }
    public int affection() { return affection; }
    public List<MoodModifier> activeModifiers() { return List.copyOf(activeModifiers); }
}
