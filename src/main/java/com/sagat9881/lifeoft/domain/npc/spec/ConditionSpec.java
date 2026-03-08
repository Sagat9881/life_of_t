package com.sagat9881.lifeoft.domain.npc.spec;

/**
 * Universal condition predicate loaded from XML.
 * Evaluated by ConditionEvaluator against NpcInstance + GameSessionContext.
 * 
 * Supported types:
 * - "mood"         : target=axis (happiness, loneliness...), operator=gte/lte/gt/lt, value=threshold
 * - "memory"       : target=check (is_being_ignored, detect_pattern, recent_interaction)
 * - "schedule"     : target=check (available, at_home, away)
 * - "stat"         : target=player stat (stress, energy, mood, money, self_esteem)
 * - "day"          : operator=gte/lte, value=day number
 * - "time"         : operator=gte/lte, value=hour
 * - "relationship" : target=axis (closeness, trust, romance, stability)
 *
 * @param type condition category
 * @param target what to check within the category
 * @param operator comparison operator or check name
 * @param value numeric threshold (nullable for non-numeric checks)
 */
public record ConditionSpec(
        String type,
        String target,
        String operator,
        Double value
) {
    public ConditionSpec {
        if (type == null || type.isBlank()) throw new IllegalArgumentException("Condition type required");
    }
}
