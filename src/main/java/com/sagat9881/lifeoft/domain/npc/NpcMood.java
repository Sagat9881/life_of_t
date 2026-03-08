package com.sagat9881.lifeoft.domain.npc;

/**
 * Six-axis mood model for NPCs.
 * Each axis ranges 0-100. Mood is recalculated daily
 * and can be overridden by extreme values.
 *
 * Axes:
 * - happiness: general well-being
 * - anxiety: worry and nervousness
 * - loneliness: need for social contact
 * - irritability: frustration threshold
 * - energy: physical/mental capacity
 * - affection: warmth toward player
 *
 * No hardcoded NPC-specific factories — all initial values come from XML spec.
 */
public record NpcMood(
        int happiness,
        int anxiety,
        int loneliness,
        int irritability,
        int energy,
        int affection
) {
    private static final int MIN = 0;
    private static final int MAX = 100;
    private static final int DECAY_RATE = 3;
    private static final int LONELINESS_GROWTH = 5;

    /**
     * Create mood from XML spec initial values.
     */
    public static NpcMood fromSpec(NpcSpec spec) {
        NpcSpecLoader.NpcMoodInitial init = spec.moodInitial();
        if (init == null) {
            return new NpcMood(50, 20, 20, 10, 70, 50);
        }
        return new NpcMood(
                init.happiness(), init.anxiety(), init.loneliness(),
                init.irritability(), init.energy(), init.affection()
        );
    }

    /**
     * Daily tick — mood decays toward baseline.
     * Loneliness grows if not addressed.
     * Energy partially recovers.
     */
    public NpcMood dailyTick() {
        return new NpcMood(
                decayToward(happiness, 50, DECAY_RATE),
                decayToward(anxiety, 20, DECAY_RATE),
                clamp(loneliness + LONELINESS_GROWTH),
                decayToward(irritability, 15, DECAY_RATE),
                clamp(Math.min(energy + 10, MAX)),
                decayToward(affection, 40, 2)
        );
    }

    /**
     * Overall urgency score — how much this NPC "needs" to act.
     * Used by UtilityBrain to weight action evaluation.
     */
    public double urgencyScore() {
        return (loneliness * 0.3 + irritability * 0.25
                + anxiety * 0.2 + (MAX - happiness) * 0.15
                + (MAX - energy) * 0.1) / MAX;
    }

    /**
     * Check if any mood axis is at an extreme that should override schedule.
     */
    public boolean hasExtremeState() {
        return irritability > 75 || loneliness > 80
                || anxiety > 70 || energy < 15;
    }

    // Fluent withers for individual axis updates
    public NpcMood withHappiness(int v) {
        return new NpcMood(clamp(v), anxiety, loneliness, irritability, energy, affection);
    }

    public NpcMood withAnxiety(int v) {
        return new NpcMood(happiness, clamp(v), loneliness, irritability, energy, affection);
    }

    public NpcMood withLoneliness(int v) {
        return new NpcMood(happiness, anxiety, clamp(v), irritability, energy, affection);
    }

    public NpcMood withIrritability(int v) {
        return new NpcMood(happiness, anxiety, loneliness, clamp(v), energy, affection);
    }

    public NpcMood withEnergy(int v) {
        return new NpcMood(happiness, anxiety, loneliness, irritability, clamp(v), affection);
    }

    public NpcMood withAffection(int v) {
        return new NpcMood(happiness, anxiety, loneliness, irritability, energy, clamp(v));
    }

    private static int clamp(int value) {
        return Math.max(MIN, Math.min(MAX, value));
    }

    private static int decayToward(int current, int baseline, int rate) {
        if (current > baseline) {
            return Math.max(baseline, current - rate);
        } else if (current < baseline) {
            return Math.min(baseline, current + rate);
        }
        return current;
    }
}
