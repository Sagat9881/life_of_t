package com.sagat9881.lifeoft.domain.npc.model;

/**
 * NPC mood with 6 independent axes.
 * All values range 0-100.
 * Initial values come from XML spec, not hardcoded.
 * Named NPCs use all 6 axes; filler NPCs use only happiness + energy.
 */
public class NpcMood {

    private double happiness;
    private double anxiety;
    private double loneliness;
    private double irritability;
    private double energy;
    private double affection;

    public NpcMood(double happiness, double anxiety, double loneliness,
                   double irritability, double energy, double affection) {
        this.happiness = clamp(happiness);
        this.anxiety = clamp(anxiety);
        this.loneliness = clamp(loneliness);
        this.irritability = clamp(irritability);
        this.energy = clamp(energy);
        this.affection = clamp(affection);
    }

    public double happiness() { return happiness; }
    public double anxiety() { return anxiety; }
    public double loneliness() { return loneliness; }
    public double irritability() { return irritability; }
    public double energy() { return energy; }
    public double affection() { return affection; }

    public NpcMood withHappiness(double v) {
        return new NpcMood(v, anxiety, loneliness, irritability, energy, affection);
    }
    public NpcMood withAnxiety(double v) {
        return new NpcMood(happiness, v, loneliness, irritability, energy, affection);
    }
    public NpcMood withLoneliness(double v) {
        return new NpcMood(happiness, anxiety, v, irritability, energy, affection);
    }
    public NpcMood withIrritability(double v) {
        return new NpcMood(happiness, anxiety, loneliness, v, energy, affection);
    }
    public NpcMood withEnergy(double v) {
        return new NpcMood(happiness, anxiety, loneliness, irritability, v, affection);
    }
    public NpcMood withAffection(double v) {
        return new NpcMood(happiness, anxiety, loneliness, irritability, energy, v);
    }

    /**
     * Urgency score: how strongly this NPC wants to act.
     * Higher values = more likely to override schedule with proactive action.
     */
    public double urgencyScore() {
        return (loneliness * 0.3) + (irritability * 0.25) + (anxiety * 0.2)
                + ((100 - happiness) * 0.15) + ((100 - energy) * 0.1);
    }

    /**
     * Is any mood axis at extreme level (>= threshold)?
     * Used by schedule override logic.
     */
    public boolean hasExtremeAxis(double threshold) {
        return loneliness >= threshold || irritability >= threshold
                || anxiety >= threshold || (100 - happiness) >= threshold;
    }

    /**
     * Apply interaction effect: player interacted with this NPC.
     */
    public NpcMood onPlayerInteraction(double qualityMultiplier) {
        return new NpcMood(
                clamp(happiness + 8 * qualityMultiplier),
                clamp(anxiety - 5 * qualityMultiplier),
                clamp(loneliness - 15 * qualityMultiplier),
                clamp(irritability - 5 * qualityMultiplier),
                clamp(energy - 3),
                clamp(affection + 5 * qualityMultiplier)
        );
    }

    private static double clamp(double v) {
        return Math.max(0, Math.min(100, v));
    }

    @Override
    public String toString() {
        return String.format("NpcMood[hap=%.0f, anx=%.0f, lon=%.0f, irr=%.0f, ene=%.0f, aff=%.0f]",
                happiness, anxiety, loneliness, irritability, energy, affection);
    }
}
