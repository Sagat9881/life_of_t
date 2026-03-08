package ru.lifegame.backend.domain.npc.spec;

import ru.lifegame.backend.domain.npc.spec.ConditionSpec;

import java.util.List;

public record ScoredAction(
    String actionId,
    double baseScore,
    String eventType,
    List<ConditionSpec> conditions,
    List<ActionOption> options
) {
    public record ActionOption(
        String optionId,
        String text,
        String resultText,
        int energyDelta,
        int stressDelta,
        int moodDelta,
        int moneyDelta,
        String relationshipTarget,
        int relationshipDelta
    ) {}
}
