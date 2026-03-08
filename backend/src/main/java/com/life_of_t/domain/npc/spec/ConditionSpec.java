package com.life_of_t.domain.npc.spec;

/**
 * Universal condition specification loaded from XML.
 * The ConditionEvaluator interprets these against NpcInstance + context.
 * Backend knows nothing about specific condition semantics —
 * it only matches type/operator/value against runtime state.
 *
 * Supported types (extensible via ConditionEvaluator):
 *  - mood: check NPC mood axis (axis, operator, value)
 *  - memory: check NPC memory pattern (action, min-count)
 *  - schedule: check NPC availability (check="available")
 *  - stat: check player stat (target, operator, value)
 *  - day: check game day (operator, value)
 *  - relationship: check player-NPC relationship (target, operator, value)
 *  - npc_mood: check another NPC's mood (npc, axis, operator, value)
 */
public record ConditionSpec(
        String type,
        String axis,
        String operator,
        String value,
        String check,
        String npc,
        String action,
        String minCount
) {

    public int intValue() {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int intMinCount() {
        try {
            return Integer.parseInt(minCount);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
