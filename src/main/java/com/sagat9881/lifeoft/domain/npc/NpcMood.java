package com.sagat9881.lifeoft.domain.npc;

import com.sagat9881.lifeoft.domain.npc.spec.NpcSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 6-axis mood system for NPC. All axes range 0-100.
 * Axes: happiness, anxiety, loneliness, irritability, energy, affection.
 *
 * For NAMED NPCs all 6 axes are active.
 * For FILLER NPCs only happiness + energy are used (others default to 50).
 *
 * Mood is initialized from XML spec via fromSpec().
 * No hardcoded NPC names — the engine doesn't know who "alexander" is.
 */
public class NpcMood {

    private static final int MIN = 0;
    private static final int MAX = 100;
    private static final int BASE_DECAY = 2;

    private final Map<String, Integer> axes;
    private final List<NpcMoodModifier> activeModifiers;
    private final boolean fullBrain;

    private NpcMood(Map<String, Integer> axes, boolean fullBrain) {
        this.axes = new HashMap<>(axes);
        this.activeModifiers = new ArrayList<>();
        this.fullBrain = fullBrain;
    }

    /**
     * Creates mood from XML spec initial values.
     * Named NPCs get full 6-axis brain, fillers get simplified.
     */
    public static NpcMood fromSpec(NpcSpec spec) {
        Map<String, Integer> axes = new HashMap<>();
        boolean isNamed = spec.isNamed();

        axes.put("happiness", spec.initialMood("happiness"));
        axes.put("energy", spec.initialMood("energy"));

        if (isNamed) {
            axes.put("anxiety", spec.initialMood("anxiety"));
            axes.put("loneliness", spec.initialMood("loneliness"));
            axes.put("irritability", spec.initialMood("irritability"));
            axes.put("affection", spec.initialMood("affection"));
        } else {
            axes.put("anxiety", 50);
            axes.put("loneliness", 50);
            axes.put("irritability", 50);
            axes.put("affection", 50);
        }

        return new NpcMood(axes, isNamed);
    }

    public int axis(String name) {
        return axes.getOrDefault(name, 50);
    }

    public int happiness() { return axis("happiness"); }
    public int anxiety() { return axis("anxiety"); }
    public int loneliness() { return axis("loneliness"); }
    public int irritability() { return axis("irritability"); }
    public int energy() { return axis("energy"); }
    public int affection() { return axis("affection"); }

    public void adjust(String axisName, int delta) {
        int current = axes.getOrDefault(axisName, 50);
        axes.put(axisName, clamp(current + delta));
    }

    public void addModifier(NpcMoodModifier modifier) {
        activeModifiers.add(modifier);
    }

    /**
     * Daily tick: apply base decay toward neutral (50),
     * then apply all active modifiers, then expire spent modifiers.
     */
    public void dailyTick() {
        // Base decay toward 50
        for (var entry : axes.entrySet()) {
            int val = entry.getValue();
            if (val > 50) {
                entry.setValue(Math.max(50, val - BASE_DECAY));
            } else if (val < 50) {
                entry.setValue(Math.min(50, val + BASE_DECAY));
            }
        }

        // Apply active modifiers
        for (NpcMoodModifier mod : activeModifiers) {
            if (!mod.isExpired()) {
                adjust(mod.axis(), mod.delta());
            }
        }

        // Tick and expire modifiers
        List<NpcMoodModifier> updated = new ArrayList<>();
        for (NpcMoodModifier mod : activeModifiers) {
            NpcMoodModifier ticked = mod.tick();
            if (!ticked.isExpired()) {
                updated.add(ticked);
            }
        }
        activeModifiers.clear();
        activeModifiers.addAll(updated);
    }

    /**
     * Returns the dominant extreme mood axis (>= threshold) or null.
     * Used for mood-override in schedule: if irritability > 70,
     * NPC might leave dinner and go for a walk.
     */
    public String dominantExtreme(int threshold) {
        if (!fullBrain) return null;
        String dominant = null;
        int maxDeviation = 0;
        for (var entry : axes.entrySet()) {
            int deviation = Math.abs(entry.getValue() - 50);
            if (entry.getValue() >= threshold && deviation > maxDeviation) {
                dominant = entry.getKey();
                maxDeviation = deviation;
            }
        }
        return dominant;
    }

    /**
     * Urgency score for Utility AI — higher means NPC needs attention more.
     */
    public double urgencyScore() {
        double score = 0;
        score += Math.max(0, loneliness() - 40) * 0.3;
        score += Math.max(0, irritability() - 40) * 0.2;
        score += Math.max(0, anxiety() - 40) * 0.2;
        score += Math.max(0, 40 - happiness()) * 0.2;
        score += Math.max(0, 30 - energy()) * 0.1;
        return score;
    }

    /**
     * Simplified mood label for frontend rendering.
     */
    public String moodSummary() {
        if (irritability() > 60) return "irritated";
        if (anxiety() > 60) return "anxious";
        if (loneliness() > 60) return "lonely";
        if (happiness() > 60) return "happy";
        if (happiness() < 30) return "sad";
        if (energy() < 25) return "tired";
        return "neutral";
    }

    private static int clamp(int val) {
        return Math.max(MIN, Math.min(MAX, val));
    }
}
