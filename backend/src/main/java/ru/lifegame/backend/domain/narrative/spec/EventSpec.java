package ru.lifegame.backend.domain.narrative.spec;

import java.util.List;

/**
 * Domain model for a narrative event loaded from narrative/events/*.xml.
 *
 * <p>Now implements {@link NarrativeSpec} directly.
 * {@link #getId()} returns the XML {@code id} attribute.
 * {@link #getBlockId()} returns the fixed block identifier {@code "events"}.
 *
 * <p>Ref: java-developer-skill.md §7. TASK-BE-018.
 */
public record EventSpec(
        String id,
        EventMeta meta,
        List<ConditionSpec> conditions,
        List<DialogueLine> dialogue,
        List<OptionSpec> options
) implements NarrativeSpec {

    // ── NarrativeSpec ─────────────────────────────────────────────────────────

    @Override
    public String getId() { return id; }

    @Override
    public String getBlockId() { return "events"; }

    // ── nested records ────────────────────────────────────────────────────────

    /** Core metadata block. */
    public record EventMeta(
            String titleRu,
            String descriptionRu,
            String type,
            double probability,
            int    cooldownHours
    ) {}

    /**
     * Trigger condition.
     * type:  time_of_day | stat_min | location | trigger | weather | season
     */
    public record ConditionSpec(String type, String stat, String value) {
        public int intValue() {
            try { return Integer.parseInt(value); }
            catch (NumberFormatException e) { return 0; }
        }
    }

    /** A single dialogue line rendered before the choice buttons. */
    public record DialogueLine(String speaker, String textRu) {}

    /** One choice presented to the player. */
    public record OptionSpec(String id, String labelRu, List<EffectSpec> effects) {}

    /** A single effect inside an option. */
    public record EffectSpec(String type, String target, String value) {
        public int intValue() {
            try { return Integer.parseInt(value); }
            catch (NumberFormatException e) { return 0; }
        }
    }
}
