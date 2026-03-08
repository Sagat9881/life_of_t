package com.sagat9881.lifeoft.domain.npc.model;

import java.util.*;

/**
 * 6-axis mood model for NPC.
 * Axes: happiness, anxiety, loneliness, irritability, energy, affection.
 * 
 * Named NPCs use all 6 axes.
 * Filler NPCs use simplified 2-axis (happiness + energy only).
 * 
 * All values range 0-100. Created from XML spec initial values.
 */
public class NpcMood {

    private final Map<String, Double> axes;
    private final boolean simplified;

    public static final List<String> ALL_AXES = List.of(
            "happiness", "anxiety", "loneliness",
            "irritability", "energy", "affection"
    );

    public static final List<String> SIMPLIFIED_AXES = List.of("happiness", "energy");

    private NpcMood(Map<String, Double> axes, boolean simplified) {
        this.axes = new LinkedHashMap<>(axes);
        this.simplified = simplified;
    }

    /**
     * Create full 6-axis mood from XML spec values.
     */
    public static NpcMood fromSpec(Map<String, Double> initial) {
        Map<String, Double> axes = new LinkedHashMap<>();
        for (String axis : ALL_AXES) {
            axes.put(axis, initial.getOrDefault(axis, 50.0));
        }
        return new NpcMood(axes, false);
    }

    /**
     * Create simplified 2-axis mood for filler NPCs.
     */
    public static NpcMood simplified(double happiness, double energy) {
        Map<String, Double> axes = new LinkedHashMap<>();
        axes.put("happiness", happiness);
        axes.put("energy", energy);
        return new NpcMood(axes, true);
    }

    public double getAxis(String axis) {
        return axes.getOrDefault(axis, 50.0);
    }

    public void adjustAxis(String axis, double delta) {
        if (axes.containsKey(axis)) {
            double current = axes.get(axis);
            axes.put(axis, clamp(current + delta));
        }
    }

    /**
     * Daily decay: each axis drifts toward 50 by decay rate.
     * Decay rates come from XML spec.
     */
    public void dailyTick(Map<String, Double> decayRates) {
        for (String axis : axes.keySet()) {
            double rate = decayRates.getOrDefault(axis, 5.0);
            double current = axes.get(axis);
            double drift = (50.0 - current) * (rate / 100.0);
            axes.put(axis, clamp(current + drift));
        }
    }

    /**
     * Check if any mood axis is at extreme level (above threshold).
     */
    public boolean isExtreme(String axis, double threshold) {
        return getAxis(axis) >= threshold;
    }

    /**
     * Compute overall urgency score — how much this NPC "needs" attention.
     * Higher = more urgent.
     */
    public double urgencyScore() {
        double loneliness = getAxis("loneliness");
        double anxiety = getAxis("anxiety");
        double irritability = getAxis("irritability");
        double happiness = getAxis("happiness");
        return (loneliness * 0.35 + anxiety * 0.25 + irritability * 0.25 + (100 - happiness) * 0.15);
    }

    public boolean isSimplified() {
        return simplified;
    }

    public Map<String, Double> allAxes() {
        return Collections.unmodifiableMap(axes);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }

    @Override
    public String toString() {
        return "NpcMood" + axes;
    }
}
