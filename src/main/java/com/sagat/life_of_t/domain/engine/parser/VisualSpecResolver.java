package com.sagat.life_of_t.domain.engine.parser;

import com.sagat.life_of_t.domain.engine.spec.VisualSpec;
import com.sagat.life_of_t.domain.engine.spec.VisualSpec.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Resolves visual spec inheritance. Takes a concrete spec with
 * {@code extends="abstract/entities/human@1.0"} and merges it
 * with the abstract base to produce a fully resolved, flat spec.
 * <p>
 * Resolution rules:
 * <ul>
 *   <li>Color palette: concrete overrides base entries by key</li>
 *   <li>Layers with replace=true fully replace the base layer with same id</li>
 *   <li>Layers not present in concrete are inherited from base</li>
 *   <li>Animations from base are inherited; extras from concrete are appended</li>
 *   <li>Canvas and displayScale from concrete override base if present</li>
 * </ul>
 */
public class VisualSpecResolver {

    private final Path assetSpecsRoot;
    private final VisualSpecParser parser;
    private final Map<String, VisualSpec> abstractCache = new HashMap<>();

    public VisualSpecResolver(Path assetSpecsRoot) {
        this.assetSpecsRoot = assetSpecsRoot;
        this.parser = new VisualSpecParser();
    }

    /**
     * Resolves a concrete spec by merging with its abstract base.
     * If the spec has no extends reference, returns it unchanged.
     */
    public VisualSpec resolve(VisualSpec concrete) throws Exception {
        if (concrete.extendsRef() == null || concrete.extendsRef().isBlank()) {
            return concrete;
        }

        ExtendsRef ref = ExtendsRef.parse(concrete.extendsRef());
        VisualSpec base = loadAbstract(ref);
        validateVersion(ref, base);
        return merge(base, concrete);
    }

    /**
     * Resolves a spec file from disk, including inheritance.
     */
    public VisualSpec resolveFromFile(Path specFile) throws Exception {
        try (InputStream is = Files.newInputStream(specFile)) {
            VisualSpec raw = parser.parse(is);
            return resolve(raw);
        }
    }

    /**
     * Resolves and writes a flat XML to the output stream.
     * Useful for debugging — shows the fully merged spec.
     */
    public void resolveAndWrite(Path specFile, OutputStream out) throws Exception {
        VisualSpec resolved = resolveFromFile(specFile);
        VisualSpecXmlWriter.write(resolved, out);
    }

    private VisualSpec loadAbstract(ExtendsRef ref) throws Exception {
        String cacheKey = ref.path() + "@" + ref.majorVersion();
        if (abstractCache.containsKey(cacheKey)) {
            return abstractCache.get(cacheKey);
        }

        Path abstractFile = assetSpecsRoot.resolve(ref.path())
                .resolve("visual-specs.xml");
        if (!Files.exists(abstractFile)) {
            throw new IllegalStateException(
                    "Abstract spec not found: " + abstractFile);
        }

        try (InputStream is = Files.newInputStream(abstractFile)) {
            VisualSpec base = parser.parse(is);
            if (!base.isAbstract()) {
                throw new IllegalStateException(
                        "Referenced spec is not abstract: " + ref.path());
            }
            abstractCache.put(cacheKey, base);
            return base;
        }
    }

    private void validateVersion(ExtendsRef ref, VisualSpec base) {
        if (ref.majorVersion() == 0) return;

        String baseVersion = base.specVersion();
        if (baseVersion == null || baseVersion.isBlank()) return;

        int baseMajor = parseMajor(baseVersion);
        if (baseMajor != ref.majorVersion()) {
            throw new IllegalStateException(String.format(
                    "Version mismatch: spec requires %s@%d but found base v%s",
                    ref.path(), ref.majorVersion(), baseVersion));
        }
    }

    private VisualSpec merge(VisualSpec base, VisualSpec concrete) {
        Map<String, String> mergedPalette = mergePalette(
                base.colorPalette(), concrete.colorPalette());

        CanvasSpec canvas = concrete.canvas() != null
                ? concrete.canvas() : base.canvas();
        double scale = concrete.displayScale() > 0
                ? concrete.displayScale() : base.displayScale();

        List<LayerSpec> mergedLayers = mergeLayers(
                base.layers(), concrete.layers());
        List<AnimationSpec> mergedAnims = mergeAnimations(
                base.animations(), concrete.animations());

        return new VisualSpec(
                concrete.entityName(),
                concrete.entityType(),
                concrete.specVersion(),
                false,
                null,
                canvas, scale,
                Collections.unmodifiableMap(mergedPalette),
                Collections.unmodifiableList(mergedLayers),
                Collections.unmodifiableList(mergedAnims)
        );
    }

    private Map<String, String> mergePalette(
            Map<String, String> base, Map<String, String> override) {
        Map<String, String> merged = new LinkedHashMap<>(base);
        merged.putAll(override);
        return merged;
    }

    private List<LayerSpec> mergeLayers(
            List<LayerSpec> baseLayers, List<LayerSpec> concreteLayers) {
        Map<String, LayerSpec> layerMap = new LinkedHashMap<>();
        for (LayerSpec layer : baseLayers) {
            layerMap.put(layer.id(), layer);
        }
        for (LayerSpec layer : concreteLayers) {
            if (layer.replace() || !layerMap.containsKey(layer.id())) {
                layerMap.put(layer.id(), layer);
            }
        }
        return layerMap.values().stream()
                .sorted(Comparator.comparingInt(LayerSpec::zOrder))
                .collect(Collectors.toList());
    }

    private List<AnimationSpec> mergeAnimations(
            List<AnimationSpec> baseAnims, List<AnimationSpec> concreteAnims) {
        Map<String, AnimationSpec> animMap = new LinkedHashMap<>();
        for (AnimationSpec anim : baseAnims) {
            animMap.put(anim.name(), anim);
        }
        for (AnimationSpec anim : concreteAnims) {
            animMap.put(anim.name(), anim);
        }
        return new ArrayList<>(animMap.values());
    }

    private int parseMajor(String version) {
        try {
            String[] parts = version.split("\\.");
            return Integer.parseInt(parts[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Parsed extends reference: "abstract/entities/human@1" -> path + version.
     */
    record ExtendsRef(String path, int majorVersion) {
        static ExtendsRef parse(String raw) {
            int atIdx = raw.indexOf('@');
            if (atIdx < 0) {
                return new ExtendsRef(raw, 0);
            }
            String path = raw.substring(0, atIdx);
            int ver = 0;
            try {
                ver = Integer.parseInt(raw.substring(atIdx + 1).split("\\.")[0]);
            } catch (NumberFormatException ignored) {}
            return new ExtendsRef(path, ver);
        }
    }
}
