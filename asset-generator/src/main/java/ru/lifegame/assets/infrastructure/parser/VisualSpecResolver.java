package ru.lifegame.assets.infrastructure.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.assets.domain.model.asset.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves inheritance for visual-specs that use extends="..."
 * Loads abstract parent specs, validates version compatibility,
 * and merges layers, animations, and color palettes.
 *
 * Thread-safe: caches parsed parent specs to avoid re-parsing.
 */
public class VisualSpecResolver {

    private static final Logger log = LoggerFactory.getLogger(VisualSpecResolver.class);

    private final Path specsRoot;
    private final XmlAssetSpecParser parser;
    private final Map<String, AssetSpec> cache = new ConcurrentHashMap<>();

    public VisualSpecResolver(Path specsRoot, XmlAssetSpecParser parser) {
        this.specsRoot = specsRoot;
        this.parser = parser;
    }

    /**
     * Loads and caches the parent abstract spec referenced by extendsRef.
     * Format: "abstract/entities/human@1.0" or "abstract/entities/human"
     *
     * @param extendsRef the extends attribute value
     * @return parsed parent AssetSpec
     * @throws XmlParseException if parent cannot be found or version mismatch
     */
    public AssetSpec loadParent(String extendsRef) {
        String path = extendsRef;
        String requiredMajor = null;

        int atIdx = extendsRef.indexOf('@');
        if (atIdx > 0) {
            path = extendsRef.substring(0, atIdx);
            String versionStr = extendsRef.substring(atIdx + 1);
            requiredMajor = versionStr.contains(".")
                    ? versionStr.substring(0, versionStr.indexOf('.'))
                    : versionStr;
        }

        String finalRequiredMajor = requiredMajor;
        return cache.computeIfAbsent(path, key -> {
            Path specFile = specsRoot.resolve(key).resolve("visual-specs.xml");
            if (!Files.exists(specFile)) {
                throw new XmlParseException(
                        "Abstract spec not found: " + specFile + " (referenced by extends=\"" + extendsRef + "\")");
            }
            log.info("Loading abstract spec: {}", specFile);
            AssetSpec parentSpec = parser.parse(specFile);

            if (finalRequiredMajor != null) {
                String parentVersion = parentSpec.version();
                String parentMajor = parentVersion.contains(".")
                        ? parentVersion.substring(0, parentVersion.indexOf('.'))
                        : parentVersion;
                if (!parentMajor.equals(finalRequiredMajor)) {
                    throw new XmlParseException(
                            "Version mismatch: child requires major " + finalRequiredMajor
                                    + " but parent " + key + " is version " + parentVersion);
                }
            }

            return parentSpec;
        });
    }

    public List<AssetLayer> mergeLayers(
            List<AssetLayer> parentLayers,
            List<LayerOverride> overrides,
            List<AssetLayer> ownLayers,
            Map<String, String> colorOverrides,
            List<ColorRemap> colorRemaps) {

        Map<String, AssetLayer> layerMap = new LinkedHashMap<>();
        for (AssetLayer layer : parentLayers) {
            layerMap.put(layer.id(), layer);
        }

        // Apply layer overrides
        for (LayerOverride override : overrides) {
            if (override.replace()) {
                AssetLayer original = layerMap.get(override.id());
                String type = override.type().isBlank()
                        ? (original != null ? original.type() : "base")
                        : override.type();
                int zOrder = override.zOrder() >= 0
                        ? override.zOrder()
                        : (original != null ? original.zOrder() : 0);
                int width = override.width() > 0
                        ? override.width()
                        : (original != null ? original.width() : 0);
                int height = override.height() > 0
                        ? override.height()
                        : (original != null ? original.height() : 0);
                // Resolve follows: child override wins if present, else inherit from parent
                String follows = override.follows() != null
                        ? override.follows()
                        : (original != null ? original.follows() : null);
                List<LayerCondition> conditions = override.conditions().isEmpty()
                        ? (original != null ? original.conditions() : List.of())
                        : override.conditions();

                layerMap.put(override.id(), new AssetLayer(
                        override.id(), type, "", zOrder,
                        width, height, override.pixelData(), follows, conditions));
            } else if (layerMap.containsKey(override.id())) {
                AssetLayer original = layerMap.get(override.id());
                PixelData merged = mergePixelData(original.pixelData(), override.pixelData());
                // Preserve follows from override if set, else keep original
                String follows = override.follows() != null
                        ? override.follows() : original.follows();
                layerMap.put(override.id(), new AssetLayer(
                        original.id(), original.type(), original.description(),
                        original.zOrder(), original.width(), original.height(),
                        merged, follows, original.conditions()));
            }
        }

        // Add own layers not already present
        for (AssetLayer own : ownLayers) {
            if (!layerMap.containsKey(own.id())) {
                layerMap.put(own.id(), own);
            }
        }

        // Apply color remaps to all layers
        List<AssetLayer> result = new ArrayList<>();
        for (AssetLayer layer : layerMap.values()) {
            PixelData remapped = applyColorRemaps(layer.pixelData(), colorRemaps);
            result.add(new AssetLayer(
                    layer.id(), layer.type(), layer.description(),
                    layer.zOrder(), layer.width(), layer.height(),
                    remapped, layer.follows(), layer.conditions()));
        }

        return result;
    }


    /**
     * Merges parent animations with child extra animations.
     * Child extras with same name as parent override the parent version.
     */
    public List<AnimationSpec> mergeAnimations(
            List<AnimationSpec> parentAnimations,
            List<AnimationSpec> extraAnimations) {

        Map<String, AnimationSpec> animMap = new LinkedHashMap<>();
        for (AnimationSpec anim : parentAnimations) {
            animMap.put(anim.name(), anim);
        }
        for (AnimationSpec extra : extraAnimations) {
            animMap.put(extra.name(), extra);
        }
        return new ArrayList<>(animMap.values());
    }

    /**
     * Merges palettes: child overrides parent if child has non-empty lists.
     */
    public ColorPalette mergePalette(ColorPalette parent, ColorPalette child) {
        List<String> primary = child.primary().isEmpty() ? parent.primary() : child.primary();
        List<String> secondary = child.secondary().isEmpty() ? parent.secondary() : child.secondary();
        return new ColorPalette(primary, secondary);
    }

    // ---- Private helpers ----

    private PixelData mergePixelData(PixelData parent, PixelData child) {
        List<PixelRect> rects = new ArrayList<>(parent.rects());
        rects.addAll(child.rects());
        List<PixelLine> lines = new ArrayList<>(parent.lines());
        lines.addAll(child.lines());
        List<PixelDot> dots = new ArrayList<>(parent.dots());
        dots.addAll(child.dots());
        return new PixelData(rects, lines, dots);
    }

    private PixelData applyColorRemaps(PixelData data, List<ColorRemap> remaps) {
        if (remaps.isEmpty()) return data;

        Map<String, String> remapMap = new HashMap<>();
        for (ColorRemap r : remaps) {
            remapMap.put(r.from().toUpperCase(), r.to());
        }

        List<PixelRect> rects = data.rects().stream()
                .map(r -> new PixelRect(r.x(), r.y(), r.w(), r.h(),
                        remapColor(r.color(), remapMap)))
                .toList();
        List<PixelLine> lines = data.lines().stream()
                .map(l -> new PixelLine(l.x(), l.y(), l.length(), l.direction(),
                        remapColor(l.color(), remapMap)))
                .toList();
        List<PixelDot> dots = data.dots().stream()
                .map(d -> new PixelDot(d.x(), d.y(),
                        remapColor(d.color(), remapMap)))
                .toList();
        return new PixelData(rects, lines, dots);
    }

    private String remapColor(String color, Map<String, String> remapMap) {
        if (color == null || color.isBlank()) return color;
        String upper = color.toUpperCase();
        return remapMap.getOrDefault(upper, color);
    }
}
