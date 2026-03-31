package ru.lifegame.backend.infrastructure.spec;

import ru.lifegame.backend.domain.narrative.spec.NarrativeSpec;
import ru.lifegame.backend.domain.narrative.spec.SpecDeserializer;
import ru.lifegame.backend.domain.narrative.spec.SpecLoadException;
import ru.lifegame.backend.domain.narrative.spec.SpecPath;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Generic narrative spec loader.
 *
 * <p>Loads one or many {@link NarrativeSpec} instances from classpath resources
 * described by a {@link SpecPath}. All file discovery and parsing are delegated
 * to an injected {@link SpecDeserializer} — this class contains zero knowledge
 * of concrete spec types, entity names or XML structure.
 *
 * <h3>Key design invariants (java-developer-skill.md §5.1, §7)</h3>
 * <ul>
 *   <li>No {@code switch/if} on entity names, block IDs or file names.</li>
 *   <li>No hardcoded classpath strings — all paths come from {@link SpecPath}.</li>
 *   <li>Deserializer is provided via constructor (Dependency Injection).</li>
 *   <li>Lives in {@code infrastructure/spec/} per ADR-001 (was incorrectly in domain).</li>
 * </ul>
 *
 * <h3>Usage examples</h3>
 * <pre>{@code
 * // Load all NPC specs:
 * SpecLoader<NpcSpec> npcLoader = new SpecLoader<>(new XmlNpcSpecDeserializer());
 * List<NpcSpec> npcs = npcLoader.loadAll(SpecPath.allIn("npc-behavior"));
 *
 * // Load a single quest spec by name:
 * SpecLoader<QuestSpec> questLoader = new SpecLoader<>(new XmlQuestSpecDeserializer());
 * QuestSpec quest = questLoader.load(SpecPath.single("quests", "quest_wedding"));
 * }</pre>
 *
 * @param <T> concrete spec type, must implement {@link NarrativeSpec}
 */
public class SpecLoader<T extends NarrativeSpec> {

    private final SpecDeserializer<T> deserializer;
    private final PathMatchingResourcePatternResolver resolver;

    /**
     * Primary constructor — deserializer is injected, no hidden dependencies.
     *
     * @param deserializer type-specific XML deserializer; must not be null
     */
    public SpecLoader(SpecDeserializer<T> deserializer) {
        this.deserializer = Objects.requireNonNull(deserializer, "deserializer must not be null");
        this.resolver = new PathMatchingResourcePatternResolver();
    }

    /**
     * Loads exactly one spec from a single-file {@link SpecPath}.
     *
     * @param path must point to a single file (no wildcards)
     * @return the loaded spec
     * @throws SpecLoadException if the file is not found, or parsing fails,
     *                           or the file contains zero or multiple specs
     */
    public T load(SpecPath path) {
        Objects.requireNonNull(path, "path must not be null");
        Resource[] resources = resolve(path);
        if (resources.length == 0) {
            throw new SpecLoadException(path.classpathPattern(),
                    "No resource found for pattern: " + path.classpathPattern());
        }
        if (resources.length > 1) {
            throw new SpecLoadException(path.classpathPattern(),
                    "Expected exactly 1 resource but found " + resources.length
                    + " for pattern: " + path.classpathPattern()
                    + ". Use loadAll() for wildcard paths.");
        }
        List<T> specs = readResource(resources[0]);
        if (specs.isEmpty()) {
            throw new SpecLoadException(path.classpathPattern(),
                    "File parsed successfully but contained no spec elements.");
        }
        if (specs.size() > 1) {
            throw new SpecLoadException(path.classpathPattern(),
                    "Expected 1 spec in file but found " + specs.size()
                    + ". Use loadAll() if the file contains multiple specs.");
        }
        return specs.get(0);
    }

    /**
     * Loads all specs matching the classpath pattern in {@link SpecPath}.
     *
     * @param path may contain wildcards (e.g. {@code *.xml})
     * @return non-null, possibly empty list of all loaded specs
     * @throws SpecLoadException if any individual file fails to parse
     */
    public List<T> loadAll(SpecPath path) {
        Objects.requireNonNull(path, "path must not be null");
        Resource[] resources = resolve(path);
        List<T> result = new ArrayList<>(resources.length);
        for (Resource resource : resources) {
            result.addAll(readResource(resource));
        }
        return List.copyOf(result);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private Resource[] resolve(SpecPath path) {
        try {
            return resolver.getResources(path.classpathPattern());
        } catch (IOException e) {
            throw new SpecLoadException(path.classpathPattern(),
                    "Failed to resolve classpath pattern: " + path.classpathPattern(), e);
        }
    }

    private List<T> readResource(Resource resource) {
        String name = resource.getFilename() != null ? resource.getFilename() : resource.getDescription();
        try (InputStream is = resource.getInputStream()) {
            return deserializer.deserialize(is, name);
        } catch (SpecLoadException sle) {
            throw sle;
        } catch (Exception e) {
            throw new SpecLoadException(name, "Unexpected error reading resource: " + name, e);
        }
    }
}
