package ru.lifegame.backend.domain.engine.spec;

import java.util.List;
import java.util.Map;

public record QuestSpec(
    String id,
    QuestMeta meta,
    List<StepSpec> steps,
    List<RewardSpec> completionRewards
) {
    public record QuestMeta(String title, String description, String category, int triggerDay, List<ConditionSpec> prerequisites) {}
    public record StepSpec(String id, String type, String description, List<ObjectiveSpec> objectives, List<RewardSpec> rewards, DialogueEntry dialogue) {}
    public record ObjectiveSpec(String type, String target, int count, List<ConditionSpec> conditions) {}
    public record RewardSpec(String type, String target, int value) {}
    public record DialogueEntry(String speaker, String text, List<String> choices) {}
}
