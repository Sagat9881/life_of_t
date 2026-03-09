package ru.lifegame.backend.domain.ending.spec;

import java.util.List;

public record EndingSpec(
    String id,
    String category,
    int priority,
    EndingMeta meta,
    EndingConditions conditions
) {
    public record EndingMeta(
        String title,
        String summary,
        String epilogue
    ) {}

    public record EndingConditions(
        String mode, // AND or OR
        List<EndingCondition> conditions
    ) {}

    public record EndingCondition(
        String type,        // stat, relationship, quest, compound
        String field,       // For stat: job_satisfaction, mood, etc.
        String target,      // For relationship: HUSBAND, FATHER
        String operator,    // gte, lte, eq
        Double value,       // Threshold
        String questId,     // For quest conditions
        String state        // For quest: COMPLETED, ACTIVE
    ) {}
}
