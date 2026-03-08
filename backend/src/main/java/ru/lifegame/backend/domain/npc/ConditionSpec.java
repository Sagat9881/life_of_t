package ru.lifegame.backend.domain.npc;

/**
 * Universal condition predicate loaded from XML.
 * Evaluated by ConditionEvaluator against live NPC + game state.
 *
 * Types:
 *   mood     — check NPC mood axis (target=axis name, operator, value)
 *   memory   — check NPC memory pattern (target=pattern name like "work_obsession")
 *   schedule — check NPC availability (target="available")
 *   stat     — check player stat (target=stat name, operator, value)
 *   day      — check game day (operator, value)
 *   trait    — check NPC personality trait (target=trait name, operator, value)
 */
public record ConditionSpec(
    String type,
    String target,
    String operator,
    int value
) {
    /**
     * Shorthand constructors for common condition types.
     */
    public static ConditionSpec mood(String axis, String op, int val) {
        return new ConditionSpec("mood", axis, op, val);
    }

    public static ConditionSpec scheduleAvailable() {
        return new ConditionSpec("schedule", "available", "eq", 1);
    }

    public static ConditionSpec day(String op, int val) {
        return new ConditionSpec("day", "current", op, val);
    }

    public static ConditionSpec memory(String pattern) {
        return new ConditionSpec("memory", pattern, "eq", 1);
    }

    public static ConditionSpec stat(String statName, String op, int val) {
        return new ConditionSpec("stat", statName, op, val);
    }

    public static ConditionSpec trait(String traitName, String op, int val) {
        return new ConditionSpec("trait", traitName, op, val);
    }
}
