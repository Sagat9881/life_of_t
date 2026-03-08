package ru.lifegame.backend.domain.npc;

/**
 * Represents the internal emotional state of an NPC.
 * Updated daily based on relationship metrics + random variance.
 */
public class NpcMood {
    private int happiness;     // 0-100
    private int irritation;    // 0-100
    private int energy;        // 0-100
    private int loneliness;    // 0-100

    public NpcMood(int happiness, int irritation, int energy, int loneliness) {
        this.happiness = clamp(happiness);
        this.irritation = clamp(irritation);
        this.energy = clamp(energy);
        this.loneliness = clamp(loneliness);
    }

    public static NpcMood initialHusband() {
        return new NpcMood(70, 10, 80, 20);
    }

    public static NpcMood initialFather() {
        return new NpcMood(50, 15, 50, 40);
    }

    /**
     * Daily tick: mood drifts based on relationship closeness and days since interaction.
     */
    public NpcMood dailyTick(int closeness, int daysSinceInteraction) {
        int dLoneliness = daysSinceInteraction >= 3 ? 10 : (daysSinceInteraction >= 2 ? 5 : -5);
        int dIrritation = daysSinceInteraction >= 3 ? 8 : -3;
        int dHappiness = closeness >= 60 ? 3 : (closeness >= 40 ? 0 : -5);
        int dEnergy = (int)(Math.random() * 10) - 5; // slight random drift

        return new NpcMood(
            happiness + dHappiness,
            irritation + dIrritation,
            energy + dEnergy,
            loneliness + dLoneliness
        );
    }

    /**
     * After player interacts with this NPC, mood improves.
     */
    public NpcMood onInteraction(int qualityBonus) {
        return new NpcMood(
            happiness + 10 + qualityBonus,
            irritation - 15,
            energy + 5,
            loneliness - 20
        );
    }

    public int happiness() { return happiness; }
    public int irritation() { return irritation; }
    public int energy() { return energy; }
    public int loneliness() { return loneliness; }

    /**
     * Composite score used by BehaviorEngine to pick NPC actions.
     * High = NPC is stressed/unhappy and likely to initiate.
     */
    public int urgencyScore() {
        return (irritation + loneliness) - (happiness + energy) / 2;
    }

    public String dominantEmotion() {
        if (loneliness >= 60) return "lonely";
        if (irritation >= 60) return "irritated";
        if (happiness >= 70) return "happy";
        if (energy <= 30) return "tired";
        return "neutral";
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(100, v));
    }
}