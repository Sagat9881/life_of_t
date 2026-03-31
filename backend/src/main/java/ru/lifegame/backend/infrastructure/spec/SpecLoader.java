package ru.lifegame.backend.infrastructure.spec;

import ru.lifegame.backend.domain.narrative.spec.*;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Generic spec loader.
 *
 * <p>Supports two loading strategies:
 * <ol>
 *   <li><b>SpecPath-based</b> (original) — loads all XMLs matching a classpath glob
 *       (e.g. {@code classpath:narrative/npc-behavior/*.xml}).
 *       Preserved for backward compatibility.</li>
 *   <li><b>Manifest-driven</b> (TASK-BE-016) — receives a {@link BlockManifest}
 *       and loads each {@link SpecEntry#specPath()} individually from the classpath.
 *       This is the preferred strategy: no directory traversal, no assumptions
 *       about subdirectory structure (java-developer-skill.md §5.5).</li>
 * </ol>
 *
 * <p>The deserializer is injected — no coupling to XML, YAML, or any other format.
 *
 * <p>Ref: java-developer-skill.md §5.5, §7. TASK-BE-015, TASK-BE-016.
 *
 * @param <T> type of the spec produced by the deserializer; must implement
 *            {@link NarrativeSpec}
 */
public class SpecLoader<T extends NarrativeSpec> {

    private static final String CLASSPATH_PREFIX = "classpath:narrative/";

    private final SpecDeserializer<T> deserializer;
    private final PathMatchingResourcePatternResolver resolver;

    public SpecLoader(SpecDeserializer<T> deserializer) {
        this.deserializer = Objects.requireNonNull(deserializer, "deserializer must not be null");
        this.resolver     = new PathMatchingResourcePatternResolver();
    }

    /** Test/DI constructor with injectable resolver. */
    public SpecLoader(SpecDeserializer<T> deserializer,
                      PathMatchingResourcePatternResolver resolver) {
        this.deserializer = Objects.requireNonNull(deserializer, "deserializer must not be null");
        this.resolver     = Objects.requireNonNull(resolver, "resolver must not be null");
    }

    // ── SpecPath-based (original) API ─────────────────────────────────────────

    /**
     * Loads all specs matching the classpath pattern derived from {@code path}.
     * Pattern: {@code classpath:narrative/<blockId>/*.xml}
     * (or {@code classpath:narrative/<blockId>/<entityName>.xml} for single).
     *
     * @param path SpecPath describing block and optional entity name
     * @return all specs found; empty list if no resources match
     */
    public List<T> loadAll(SpecPath path) {
        String pattern = buildPattern(path);
        Resource[] resources;
        try {
            resources = resolver.getResources(pattern);
        } catch (IOException e) {
            throw new SpecLoadException(pattern,
                    "Failed to resolve classpath resources for pattern: " + pattern, e);
        }
        List<T> result = new ArrayList<>();
        for (Resource r : resources) {
            result.addAll(loadResource(r));
        }
        return List.copyOf(result);
    }

    /**
     * Loads a single spec. Throws {@link SpecLoadException} if not exactly one
     * resource matches or if the deserializer returns != 1 spec.
     */
    public T load(SpecPath path) {
        if (!path.isSingle())
            throw new IllegalArgumentException(
                    "load(SpecPath) requires a single-entity path; use loadAll for wildcards");
        List<T> all = loadAll(path);
        if (all.isEmpty())
            throw new SpecLoadException(path.toString(), "No spec found at: " + path);
        if (all.size() > 1)
            throw new SpecLoadException(path.toString(),
                    "Expected 1 spec but found " + all.size() + " at: " + path);
        return all.get(0);
    }

    // ── Manifest-driven API (TASK-BE-016) ─────────────────────────────────────

    /**
     * Loads all specs listed in the given {@link BlockManifest}.
     *
     * <p>Each {@link SpecEntry#specPath()} is resolved as a classpath resource:
     * {@code classpath:<specPath>}.
     * No directory scanning occurs — the manifest is the sole source of truth
     * about which entities exist (java-developer-skill.md §5.5).
     *
     * @param manifest the manifest describing the block
     * @return all specs for the block; order matches manifest entry order
     */
    public List<T> loadAll(BlockManifest manifest) {
        Objects.requireNonNull(manifest, "manifest must not be null");
        List<T> result = new ArrayList<>(manifest.size());
        for (SpecEntry entry : manifest.entries()) {
            String classpathPath = CLASSPATH_PREFIX.endsWith("/")
                    ? "classpath:" + entry.specPath()
                    : "classpath:" + entry.specPath();
            Resource resource = resolver.getResource(classpathPath);
            if (!resource.exists())
                throw new SpecLoadException(entry.specPath(),
                        "Spec file declared in manifest not found on classpath: "
                        + entry.specPath() + " (entity: " + entry.entityId() + ", block: "
                        + manifest.blockId() + ")");
            result.addAll(loadResource(resource));
        }
        return List.copyOf(result);
    }

    /**
     * Loads a single entity by its {@link SpecEntry}.
     *
     * @param entry  the manifest entry pointing to the spec file
     * @return the loaded spec
     */
    public T load(SpecEntry entry) {
        Objects.requireNonNull(entry, "entry must not be null");
        Resource resource = resolver.getResource("classpath:" + entry.specPath());
        if (!resource.exists())
            throw new SpecLoadException(entry.specPath(),
                    "Spec file not found on classpath: " + entry.specPath());
        List<T> all = loadResource(resource);
        if (all.isEmpty())
            throw new SpecLoadException(entry.specPath(),
                    "Deserializer returned empty list for: " + entry.specPath());
        return all.get(0);
    }

    // ── shared internals ──────────────────────────────────────────────────────

    private List<T> loadResource(Resource resource) {
        String name = describeResource(resource);
        try (InputStream is = resource.getInputStream()) {
            return deserializer.deserialize(is, name);
        } catch (SpecLoadException sle) {
            throw sle;
        } catch (IOException e) {
            throw new SpecLoadException(name, "Cannot open spec resource: " + name, e);
        }
    }

    private String buildPattern(SpecPath path) {
        if (path.isSingle()) {
            return "classpath:narrative/" + path.blockId() + "/" + path.entityName() + ".xml";
        }
        return "classpath:narrative/" + path.blockId() + "/*.xml";
    }

    private String describeResource(Resource resource) {
        try { return resource.getURI().toString(); }
        catch (IOException e) { return resource.getDescription(); }
    }
}
