package ru.lifegame.backend.domain.npc.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents the emotional state of an NPC.
 * Named NPCs have 6 axes; filler NPCs use only happiness and energy.
 *
 * <p>MoodModifier tracks a named effect applied to one axis for a fixed number of days.
 * dailyTick() processes modifiers in a single pass — avoiding the indexOf() bug
 * that caused duplicate modifiers to decrement the wrong entry.
 */
public class NpcMood {

    /**
     * A timed mood effect on one axis.
     *
     * @param name          human-readable label (e.g. "BONUS_HAPPINESS")
     * @param axis          one of: happiness, energy, stress, trust, romance, anger
     * @param delta         signed value applied to the axis when the modifier is active
     * @param remainingDays days left including the current day (>= 1 when created)
     */
    public record MoodModifier(String name, String axis, int delta, int remainingDays) {

        /** @return true when the modifier has expired and should be removed */
        public boolean isExpired() {
            return remainingDays <= 0;
        }

        /** @return a copy of this modifier with one less remaining day */
        public MoodModifier withDecrementedDuration() {
            return new MoodModifier(name, axis, delta, remainingDays - 1);
        }
    }

    private int happiness;
    private int energy;
    private int stress;
    private int trust;
    private int romance;
    private int anger;
    /** Active timed modifiers. Mutated only in dailyTick() and applyModifier(). */
    private final List<MoodModifier> activeModifiers;
    private final boolean fullBrain;

    private NpcMood(int happiness, int energy, int stress, int trust, int romance, int anger,
                    List<MoodModifier> activeModifiers, boolean fullBrain) {
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
        // MoodModifier is a record (immutable) — shallow copy of the list is sufficient
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
        if (anger     >= 80) return true;
        if (stress    >= 80) return true;
        if (happiness <= 20) return true;
        if (energy    <= 15) return true;
        return false;
    }

    public String dominantAxis() {
        if (anger     >= 80) return "anger";
        if (stress    >= 80) return "stress";
        if (happiness <= 20) return "happiness";
        if (energy    <= 15) return "energy";
        return "happiness";
    }

    /**
     * Called once per in-game day.
     *
     * <p>Processes active modifiers in a <em>single pass</em> (no indexOf/set):
     * <ul>
     *   <li>remainingDays &lt;= 1 → reverse the axis delta and drop the modifier.</li>
     *   <li>remainingDays &gt; 1  → keep with decremented duration.</li>
     * </ul>
     * After modifiers are settled, natural decay toward neutral is applied.
     *
     * <p>Boundary: a modifier created with remainingDays=1 is removed on the very
     * first dailyTick() (condition &lt;= 1), which is correct — it was active for
     * exactly one day.
     */
    public void dailyTick() {
        List<MoodModifier> next = new ArrayList<>();
        for (MoodModifier mod : activeModifiers) {
            if (mod.remainingDays() <= 1) {
                // Modifier expires today — reverse its contribution
                reverseDelta(mod.axis(), mod.delta());
            } else {
                // Still active — keep with one less day remaining
                next.add(mod.withDecrementedDuration());
            }
        }
        activeModifiers.clear();
        activeModifiers.addAll(next);
        decayTowardNeutral();
    }

    /**
     * Applies a timed mood modifier.
     *
     * @param modifier     label / name of the modifier
     * @param axis         target axis (happiness, energy, stress, trust, romance, anger)
     * @param delta        signed delta to apply immediately and reverse on expiry
     * @param durationDays number of days the modifier remains active (&gt;= 1)
     */
    public void applyModifier(String modifier, String axis, int delta, int durationDays) {
        applyDelta(axis, delta);
        if (modifier != null && !modifier.isBlank() && durationDays >= 1) {
            activeModifiers.add(new MoodModifier(modifier, axis, delta, durationDays));
        }
    }

    /**
     * Convenience overload — legacy callers that only affect happiness/energy/stress/anger
     * by explicit parameters (no timed duration; effect is permanent until dailyTick decay).
     */
    public void applyModifier(String modifier, int happinessDelta, int energyDelta,
                               int stressDelta, int angerDelta) {
        happiness = clamp(happiness + happinessDelta);
        energy    = clamp(energy    + energyDelta);
        stress    = clamp(stress    + stressDelta);
        anger     = clamp(anger     + angerDelta);
        // Permanent modifiers are not tracked in activeModifiers (no expiry/reversal needed)
    }

    // ── internal helpers ─────────────────────────────────────────────────────────────

    /** Natural daily drift toward neutral values. */
    private void decayTowardNeutral() {
        happiness = clamp(happiness + 2);
        energy    = clamp(energy    + 5);
        stress    = clamp(stress    - 3);
        anger     = clamp(anger     - 2);
    }

    /** Apply a signed delta to the named axis. */
    private void applyDelta(String axis, int delta) {
        switch (axis) {
            case "happiness" -> happiness = clamp(happiness + delta);
            case "energy"    -> energy    = clamp(energy    + delta);
            case "stress"    -> stress    = clamp(stress    + delta);
            case "trust"     -> trust     = clamp(trust     + delta);
            case "romance"   -> romance   = clamp(romance   + delta);
            case "anger"     -> anger     = clamp(anger     + delta);
            default -> { /* unknown axis — ignore */ }
        }
    }

    /** Reverse a previously applied delta (called when a modifier expires). */
    private void reverseDelta(String axis, int delta) {
        applyDelta(axis, -delta);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(100, v));
    }

    // ── accessors ────────────────────────────────────────────────────────────────────

    public int happiness()                        { return happiness; }
    public int energy()                           { return energy; }
    public int stress()                           { return stress; }
    public int trust()                            { return trust; }
    public int romance()                          { return romance; }
    public int anger()                            { return anger; }
    public List<MoodModifier> activeModifiers()   { return List.copyOf(activeModifiers); }
    public boolean isFullBrain()                  { return fullBrain; }
}
