package ru.lifegame.backend.domain.npc.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents the emotional state of an NPC.
 * Named NPCs have 6 axes; filler NPCs use only happiness and energy.
 */
public class NpcMood {

    private int happiness;
    private int energy;
    private int stress;
    private int trust;
    private int romance;
    private int anger;
    private final List<String> activeModifiers;
    private final boolean fullBrain;

    private NpcMood(int happiness, int energy, int stress, int trust, int romance, int anger,
                    List<String> activeModifiers, boolean fullBrain) {
        this.happiness = happiness;
        this.energy = energy;
        this.stress = stress;
        this.trust = trust;
        this.romance = romance;
        this.anger = anger;
        this.activeModifiers = new ArrayList<>(activeModifiers);
        this.fullBrain = fullBrain;
    }

    /** Copy constructor for per-session deep copy. */
    public NpcMood(NpcMood source) {
        this.happiness = source.happiness;
        this.energy = source.energy;
        this.stress = source.stress;
        this.trust = source.trust;
        this.romance = source.romance;
        this.anger = source.anger;
        this.activeModifiers = new ArrayList<>(source.activeModifiers);
        this.fullBrain = source.fullBrain;
    }

    public static NpcMood fromSpec(Map<String, Integer> initial) {
        return new NpcMood(
                initial.getOrDefault("happiness", 50),
                initial.getOrDefault("energy",    70),
                initial.getOrDefault("stress",    20),
                initial.getOrDefault("trust",     50),
                initial.getOrDefault("romance",   50),
                initial.getOrDefault("anger",     10),
                List.of(),
                true
        );
    }

    public static NpcMood fillerMood(int happiness, int energy) {
        return new NpcMood(happiness, energy, 0, 0, 0, 0, List.of(), false);
    }

    public boolean hasExtremeState() {
        if (anger    >= 80) return true;
        if (stress   >= 80) return true;
        if (happiness <= 20) return true;
        if (energy   <= 15) return true;
        return false;
    }

    public String dominantAxis() {
        if (anger    >= 80) return "anger";
        if (stress   >= 80) return "stress";
        if (happiness <= 20) return "happiness";
        if (energy   <= 15) return "energy";
        return "happiness";
    }

    public void dailyTick() {
        happiness = clamp(happiness + 2);
        energy    = clamp(energy    + 5);
        stress    = clamp(stress    - 3);
        anger     = clamp(anger     - 2);
        activeModifiers.clear();
    }

    public void applyModifier(String modifier, int happinessDelta, int energyDelta,
                               int stressDelta, int angerDelta) {
        happiness = clamp(happiness + happinessDelta);
        energy    = clamp(energy    + energyDelta);
        stress    = clamp(stress    + stressDelta);
        anger     = clamp(anger     + angerDelta);
        if (modifier != null && !modifier.isBlank()) {
            activeModifiers.add(modifier);
        }
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(100, v));
    }

    public int happiness()             { return happiness; }
    public int energy()                { return energy; }
    public int stress()                { return stress; }
    public int trust()                 { return trust; }
    public int romance()               { return romance; }
    public int anger()                 { return anger; }
    public List<String> activeModifiers() { return List.copyOf(activeModifiers); }
    public boolean isFullBrain()       { return fullBrain; }
}
