package ru.lifegame.backend.domain.conflict.spec;

import java.util.Map;

/**
 * Universal condition for conflict triggers.
 * Supports stat, relationship, time, quest, action-count, etc.
 */
public record TriggerCondition(
        String type,                    // "stat", "relationship", "time-spent", "quest", etc.
        String field,                   // "stress", "closeness", "work", etc.
        String operator,                // "gte", "lte", "eq", "between"
        Double value,                   // threshold for comparison
        String target,                  // "HUSBAND", "FATHER" (for relationship conditions)
        String questId,                 // for quest conditions
        String questState,              // "completed", "failed", "in_progress"
        Map<String, Object> params      // additional params (e.g., "min", "max" for between)
) {
    public TriggerCondition(
            String type,
            String field,
            String operator,
            Double value,
            String target,
            String questId,
            String questState,
            Map<String, Object> params
    ) {
        this.type = type;
        this.field = field;
        this.operator = operator != null ? operator : "gte";
        this.value = value;
        this.target = target;
        this.questId = questId;
        this.questState = questState;
        this.params = params != null ? params : Map.of();
    }
}
