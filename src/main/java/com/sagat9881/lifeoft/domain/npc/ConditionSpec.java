package com.sagat9881.lifeoft.domain.npc;

/**
 * Universal condition specification parsed from XML.
 * The backend doesn't know what specific conditions mean —
 * it interprets them generically via ConditionEvaluator.
 *
 * Supported types:
 * - "mood": check NPC mood axis (axis + operator + value)
 * - "memory": check NPC memory pattern (pattern)
 * - "schedule": check NPC availability (check = "available")
 * - "stat": check player stat (target + operator + value)
 * - "day": check game day (operator + value)
 * - "relationship": check relationship metric (target + operator + value)
 *
 * @param type      condition type
 * @param axis      mood axis name (for type="mood")
 * @param check     named check (for type="schedule")
 * @param operator  comparison: "gte", "lte", "eq", "gt", "lt"
 * @param value     threshold value as string
 * @param pattern   memory pattern to match (for type="memory")
 * @param target    target entity or stat name
 */
public record ConditionSpec(
        String type,
        String axis,
        String check,
        String operator,
        String value,
        String pattern,
        String target
) {
    /**
     * Parse value as integer, with default fallback.
     */
    public int intValue(int defaultVal) {
        try {
            return value != null ? Integer.parseInt(value) : defaultVal;
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    /**
     * Parse value as double, with default fallback.
     */
    public double doubleValue(double defaultVal) {
        try {
            return value != null ? Double.parseDouble(value) : defaultVal;
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
