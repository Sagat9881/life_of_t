package ru.lifegame.backend.domain.narrative.spec;

/**
 * Base contract for all narrative specifications loaded from external XML files.
 *
 * <p>Every spec type (quest, NPC, conflict, world-event) must implement this
 * interface so that {@code SpecLoader<T extends NarrativeSpec>} can operate
 * generically without any {@code switch/if} on entity names.
 *
 * <p>Ref: java-developer-skill.md §3.1 (narrative spec flow),
 *         §5.1 (no magic constants or switch-case on entity names),
 *         §7  (domain has no outbound dependencies).
 */
public interface NarrativeSpec {

    /**
     * Unique identifier of this specification instance.
     * Corresponds to the {@code id} attribute of the root XML element.
     */
    String getId();

    /**
     * Block-id of the narrative category this spec belongs to
     * (e.g. "quests", "npc-behavior", "confilcts", "events").
     * Corresponds to the {@code SpecPath#blockId()} used to load it.
     */
    String getBlockId();
}
