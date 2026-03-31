package ru.lifegame.backend.infrastructure.spec;

import ru.lifegame.backend.domain.narrative.spec.SpecPath;
import ru.lifegame.backend.domain.npc.spec.NpcSpec;
import ru.lifegame.backend.infrastructure.spec.parser.NpcSpecParser;

import java.util.List;

/**
 * Infrastructure loader for NPC specifications.
 *
 * <p>Replaces {@link ru.lifegame.backend.domain.npc.NpcSpecLoader} which was
 * incorrectly placed in the domain layer and contained a hardcoded classpath
 * string (TASK-BE-017, java-developer-skill.md §5.1, §7).
 *
 * <p>Uses {@link SpecLoader}{@code <NpcSpec>} + {@link XmlNpcSpecDeserializer}
 * so there are zero hardcoded paths and zero switch/if on entity names.
 *
 * <p>Since TASK-BE-018 {@link NpcSpec} implements
 * {@link ru.lifegame.backend.domain.narrative.spec.NarrativeSpec} directly,
 * no wrapper is needed.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * // Load all NPC specs with default block path:
 * List<NpcSpec> all = new NpcSpecLoader().loadAll();
 *
 * // Load from a custom SpecPath (e.g. test fixture):
 * List<NpcSpec> custom = new NpcSpecLoader().loadAll(SpecPath.allIn("test-npcs"));
 * }</pre>
 *
 * <p>Ref: java-developer-skill.md §5.1 (no hardcoded paths),
 *         §7 (infrastructure implements domain ports),
 *         ADR-001 TASK-BE-017.
 */
public class NpcSpecLoader {

    private static final SpecPath DEFAULT_PATH = SpecPath.allIn("npc-behavior");

    private final SpecLoader<NpcSpec> loader;

    /** Default constructor — uses {@link NpcSpecParser} and the standard classpath pattern. */
    public NpcSpecLoader() {
        this.loader = new SpecLoader<>(new XmlNpcSpecDeserializer(new NpcSpecParser()));
    }

    /** Constructor for tests / DI: inject a custom deserializer. */
    public NpcSpecLoader(XmlNpcSpecDeserializer deserializer) {
        this.loader = new SpecLoader<>(deserializer);
    }

    /**
     * Loads all NPC specs from the default classpath pattern
     * ({@code classpath:narrative/npc-behavior/*.xml}).
     *
     * @return immutable list of all loaded {@link NpcSpec} instances
     */
    public List<NpcSpec> loadAll() {
        return loader.loadAll(DEFAULT_PATH);
    }

    /**
     * Loads all NPC specs from a custom {@link SpecPath}.
     * Useful for tests or non-standard classpath layouts.
     *
     * @param path custom SpecPath; must not be null
     * @return immutable list of all loaded {@link NpcSpec} instances
     */
    public List<NpcSpec> loadAll(SpecPath path) {
        return loader.loadAll(path);
    }

    /**
     * Loads a single NPC spec by entity name.
     *
     * @param entityName file name without extension, e.g. {@code "aijan"}
     * @return the loaded {@link NpcSpec}
     */
    public NpcSpec load(String entityName) {
        return loader.load(SpecPath.single("npc-behavior", entityName));
    }
}
