package ru.lifegame.backend.domain.narrative.spec;

import java.util.List;

/**
 * Domain model for a quest loaded from narrative/quests/*.xml.
 *
 * <p>Now implements {@link NarrativeSpec} directly.
 * {@link #getId()} returns the XML {@code id} attribute.
 * {@link #getBlockId()} returns the fixed block identifier {@code "quests"}.
 *
 * <p>Ref: java-developer-skill.md §7. TASK-BE-018.
 */
public record QuestSpec(
    String id,
    QuestMeta meta,
    List<StepSpec> steps
) implements NarrativeSpec {

    // ── NarrativeSpec ─────────────────────────────────────────────────────────

    @Override
    public String getId() { return id; }

    @Override
    public String getBlockId() { return "quests"; }

    // ── nested records ────────────────────────────────────────────────────────

    public record QuestMeta(
        String title,
        String description,
        String type,
        int triggerDay,
        boolean autoStart,
        List<String> requiredNpcs
    ) {}

    public record StepSpec(
        String stepId,
        String description,
        List<ObjectiveSpec> objectives,
        List<RewardSpec> rewards,
        String dialogueText
    ) {}

    public record ObjectiveSpec(String type, String target, String operator, String value) {}
    public record RewardSpec(String type, String target, int amount) {}
    public record DialogueEntry(String speaker, String text) {}
}
