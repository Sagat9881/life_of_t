package ru.lifegame.backend.domain.engine.spec;

import java.util.List;
import java.util.Map;

public record QuestSpec(
    String id,
    QuestMeta meta,
    List<StepSpec> steps
) {
    public record QuestMeta(String title, String description, String category, int triggerDay, List<ConditionSpec> prerequisites) {}
    public record StepSpec(String id, String description, List<ObjectiveSpec> objectives, DialogueEntry dialogue, RewardSpec reward) {}
    public record ObjectiveSpec(String type, String target, int count, List<ConditionSpec> conditions) {}
    public record DialogueEntry(String speaker, String text, List<String> choices) {}
    public record RewardSpec(int energy, int stress, int mood, int money, Map<String, Integer> relationships, Map<String, Integer> skills) {}
}
