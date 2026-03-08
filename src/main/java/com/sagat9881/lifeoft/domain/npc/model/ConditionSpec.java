package com.sagat9881.lifeoft.domain.npc.model;

/**
 * Universal condition specification loaded from XML.
 * The engine's ConditionEvaluator interprets this against game state.
 * 
 * Supports multiple condition types:
 * - mood: check NPC mood axis (e.g., loneliness >= 40)
 * - memory: check NPC memory patterns (e.g., isBeingIgnored)
 * - schedule: check NPC availability
 * - stat: check player stat (e.g., stress >= 70)
 * - day: check game day (e.g., day >= 10)
 * - relationship: check player-NPC relationship (e.g., closeness >= 50)
 * 
 * @param type condition category
 * @param axis mood axis name (for mood type)
 * @param target target identifier (stat name, NPC id, etc.)
 * @param operator comparison operator: gte, lte, eq, gt, lt
 * @param value threshold value as string
 * @param check named check (for schedule: "available"; for memory: "isBeingIgnored")
 */
public record ConditionSpec(
        String type,
        String axis,
        String target,
        String operator,
        String value,
        String check
) {
}
