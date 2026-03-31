package ru.lifegame.backend.infrastructure.spec;

import ru.lifegame.backend.domain.npc.spec.NpcSpec;
import ru.lifegame.backend.domain.narrative.spec.SpecPath;

import java.util.List;

/**
 * Infrastructure entry point for loading all NPC specifications.
 *
 * <p>Moved from {@code domain/npc/NpcSpecLoader} to {@code infrastructure/spec/}
 * per ADR-001: spec loading is an infrastructure concern.
 *
 * <p>Delegates entirely to {@link SpecLoader}{@code <NpcSpec>} with
 * {@link XmlNpcSpecDeserializer} — zero knowledge of concrete NPC names,
 * file paths, or XML structure (java-developer-skill.md §5.1).
 *
 * <p>Usage:
 * <pre>{@code
 * // Via Spring DI:
 * @Bean NpcSpecLoader npcSpecLoader() { return new NpcSpecLoader(); }
 *
 * // Direct:
 * List<NpcSpec> specs = new NpcSpecLoader().loadAll();
 * }</pre>
 *
 * <p>Ref: java-developer-skill.md §3.1, §7, ADR-001, TASK-BE-017.
 */
public class NpcSpecLoader {

    private final SpecLoader<NpcSpec> loader;

    public NpcSpecLoader() {
        this.loader = new SpecLoader<>(new XmlNpcSpecDeserializer());
    }

    /** Constructor for tests / DI with a custom deserializer. */
    public NpcSpecLoader(SpecLoader<NpcSpec> loader) {
        this.loader = loader;
    }

    /**
     * Loads all NPC specs from the {@code narrative/npc-behavior/} classpath directory.
     *
     * @return non-null, unmodifiable list of all parsed {@link NpcSpec} instances
     * @throws ru.lifegame.backend.domain.narrative.spec.SpecLoadException on any parse failure
     */
    public List<NpcSpec> loadAll() {
        return loader.loadAll(SpecPath.allIn("npc-behavior"));
    }
}
