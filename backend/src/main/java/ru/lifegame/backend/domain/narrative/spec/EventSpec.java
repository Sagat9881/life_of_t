package ru.lifegame.backend.domain.narrative.spec;

import java.util.List;

/**
 * Domain model for a narrative event loaded from narrative/events/*.xml.
 *
 * <p>Implements {@link NarrativeSpec} so that {@code SpecLoader<EventSpec>}
 * can operate generically without switch/if on spec types.
 *
 * <p>Structure mirrors the XML contract:
 * <pre>
 *   &lt;event id="..." type="RANDOM|TRIGGERED|SEASONAL"&gt;
 *     &lt;meta&gt;...&lt;/meta&gt;
 *     &lt;conditions&gt;...&lt;/conditions&gt;
 *     &lt;dialogue&gt;...&lt;/dialogue&gt;
 *     &lt;options&gt;...&lt;/options&gt;
 *   &lt;/event&gt;
 * </pre>
 *
 * <p>Every event has at least one option (even if it is just a single "Ok" button).
 * Options carry their own effects — stat/relationship changes applied on choice.
 *
 * <p>Ref: java-developer-skill.md §3.1, §7, TASK-BE-018.
 */
public record EventSpec(
        String id,
        EventMeta meta,
        List<ConditionSpec> conditions,
        List<DialogueLine> dialogue,
        List<OptionSpec> options
) implements NarrativeSpec {

    @Override
    public String getId() { return id; }

    @Override
    public String getBlockId() { return "events"; }

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
     * stat:  stat name for stat_min conditions (nullable for non-stat types)
     * value: the comparison value
     */
    public record ConditionSpec(String type, String stat, String value) {
        public int intValue() {
            try { return Integer.parseInt(value); }
            catch (NumberFormatException e) { return 0; }
        }
    }

    /** A single dialogue line rendered before the choice buttons. */
    public record DialogueLine(String speaker, String textRu) {}

    /**
     * One choice presented to the player.
     * id:      machine-readable code sent back on selection
     * labelRu: button label shown to the player
     * effects: stat/relationship changes applied when this option is chosen
     */
    public record OptionSpec(String id, String labelRu, List<EffectSpec> effects) {}

    /**
     * A single effect inside an option.
     * type:   "stat-change" | "relationship-change"
     * target: stat name (for stat-change) OR NPC id (for relationship-change)
     * value:  numeric string, e.g. "+10" or "-5"
     */
    public record EffectSpec(String type, String target, String value) {
        public int intValue() {
            try { return Integer.parseInt(value); }
            catch (NumberFormatException e) { return 0; }
        }
    }
}
