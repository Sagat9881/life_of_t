package ru.lifegame.backend.infrastructure.spec;

import ru.lifegame.backend.domain.narrative.spec.BlockManifest;
import ru.lifegame.backend.domain.narrative.spec.SpecLoadException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Scans the classpath for narrative block manifests ({@code manifest.xml}) and
 * returns a {@link BlockManifest} for each one found.
 *
 * <h3>Key design guarantees (java-developer-skill.md §5.5)</h3>
 * <ul>
 *   <li>Zero hardcoded block names ({@code quest}, {@code npc}, …) — the scanner
 *       discovers all blocks by pattern, never by name.</li>
 *   <li>Adding a new block to {@code narrative/} requires only a new
 *       {@code manifest.xml}; no code change needed.</li>
 *   <li>The backend never traverses subdirectory trees for spec discovery;
 *       it reads only from manifests returned by this scanner.</li>
 * </ul>
 *
 * <h3>Classpath pattern</h3>
 * <pre>
 *   narrative/ ** /manifest.xml
 * </pre>
 * This matches exactly one manifest per block directory at any depth,
 * without assuming block names or nesting.
 *
 * <p>Ref: java-developer-skill.md §5.5, §3.1. TASK-BE-016.
 */
public class ManifestScanner {

    /** Classpath pattern that matches all narrative manifests. No hardcoded block names. */
    static final String MANIFEST_PATTERN = "classpath*:narrative/**/manifest.xml";

    private final PathMatchingResourcePatternResolver resolver;
    private final XmlManifestParser parser;

    /** Production constructor — uses Spring's classpath resolver. */
    public ManifestScanner() {
        this.resolver = new PathMatchingResourcePatternResolver();
        this.parser   = new XmlManifestParser();
    }

    /** Test/DI constructor — inject resolver and parser. */
    public ManifestScanner(PathMatchingResourcePatternResolver resolver,
                           XmlManifestParser parser) {
        this.resolver = resolver;
        this.parser   = parser;
    }

    /**
     * Scans the classpath and returns a {@link BlockManifest} for every
     * {@code manifest.xml} found under {@code narrative/}.
     *
     * <p>Order is non-deterministic (classpath resource order). Callers that
     * need a stable order should sort by {@link BlockManifest#blockId()}.
     *
     * @return immutable list of all discovered {@link BlockManifest}s
     * @throws SpecLoadException if any manifest cannot be read or parsed
     */
    public List<BlockManifest> scanAll() {
        Resource[] resources;
        try {
            resources = resolver.getResources(MANIFEST_PATTERN);
        } catch (IOException e) {
            throw new SpecLoadException(MANIFEST_PATTERN,
                    "Failed to scan for narrative manifests", e);
        }

        List<BlockManifest> manifests = new ArrayList<>(resources.length);
        for (Resource resource : resources) {
            String sourceName = describeResource(resource);
            try (InputStream is = resource.getInputStream()) {
                manifests.add(parser.parse(is, sourceName));
            } catch (SpecLoadException sle) {
                throw sle;
            } catch (IOException e) {
                throw new SpecLoadException(sourceName,
                        "Cannot open manifest resource: " + sourceName, e);
            }
        }
        return List.copyOf(manifests);
    }

    /**
     * Returns a single {@link BlockManifest} for the given blockId,
     * or throws {@link SpecLoadException} if not found.
     *
     * @param blockId e.g. {@code "quest"}, {@code "npc"}
     * @return the matching manifest
     * @throws SpecLoadException if no manifest with that blockId exists
     */
    public BlockManifest getManifest(String blockId) {
        return scanAll().stream()
                .filter(m -> blockId.equals(m.blockId()))
                .findFirst()
                .orElseThrow(() -> new SpecLoadException(blockId,
                        "No manifest found for blockId: " + blockId));
    }

    private String describeResource(Resource resource) {
        try { return resource.getURI().toString(); }
        catch (IOException e) { return resource.getDescription(); }
    }
}
