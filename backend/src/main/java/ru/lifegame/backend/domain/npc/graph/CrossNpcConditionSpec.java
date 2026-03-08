package ru.lifegame.backend.domain.npc.graph;

/**
 * A condition that checks cross-NPC relationship state.
 * Loaded from XML. Engine evaluates generically.
 *
 * Example XML:
 * <cross-condition npc-a="alexander" npc-b="father" 
 *                  axis="tension" operator="gte" value="70"
 *                  result-event="family_argument"/>
 */
public record CrossNpcConditionSpec(
        String npcA,
        String npcB,
        String axis,        // "tension", "respect", "familiarity"
        String operator,    // "gte", "lte", "eq"
        int value,
        String resultEventId // event to trigger if condition met
) {
}
