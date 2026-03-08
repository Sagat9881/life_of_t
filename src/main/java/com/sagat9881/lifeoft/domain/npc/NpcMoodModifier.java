package com.sagat9881.lifeoft.domain.npc;

/**
 * A timed mood modifier applied to an NPC mood axis.
 * Modifiers have a duration (in days) and decay each day.
 * When remainingDays reaches 0, the modifier is removed.
 *
 * @param axis mood axis this modifier affects (e.g. "happiness", "irritability")
 * @param delta amount to add/subtract each tick while active
 * @param remainingDays how many days this modifier persists
 * @param source what caused this modifier (action ID, event ID, etc.)
 */
public record NpcMoodModifier(
        String axis,
        int delta,
        int remainingDays,
        String source
) {

    public boolean isExpired() {
        return remainingDays <= 0;
    }

    public NpcMoodModifier tick() {
        return new NpcMoodModifier(axis, delta, remainingDays - 1, source);
    }

    public static NpcMoodModifier create(String axis, int delta, int durationDays, String source) {
        return new NpcMoodModifier(axis, delta, durationDays, source);
    }
}
