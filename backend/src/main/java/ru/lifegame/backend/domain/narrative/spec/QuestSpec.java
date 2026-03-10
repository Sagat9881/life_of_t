package ru.lifegame.backend.domain.narrative.spec;

import java.util.List;

public record QuestSpec(
    String id,
    QuestMeta meta,
    List<StepSpec> steps
) {
    public record QuestMeta(
        String title,
        String description,
        String type,
        int triggerDay,
        boolean autoStart,
        List<String> requiredNpcs
    ) {}
    public record StepSpec(String stepId, String description, List<ObjectiveSpec> objectives, List<RewardSpec> rewards, String dialogueText) {}
    public record ObjectiveSpec(String type, String target, String operator, String value) {}
    public record RewardSpec(String type, String target, int amount) {}
    public record DialogueEntry(String speaker, String text) {}
}
