package ru.lifegame.backend.domain.npc.spec;

import ru.lifegame.backend.domain.npc.spec.ConditionSpec;

import java.util.List;

public record ScoredAction(
    String actionId,
    double baseScore,
    List<ConditionSpec> conditions,
    List<ActionOption> options
) {
    public record ActionOption(
        String id,
        String text,
        String resultText,
        int energy,
        int stress,
        int mood,
        int money,
        String relationshipTarget,
        int relationshipDelta
    ) {}
}
