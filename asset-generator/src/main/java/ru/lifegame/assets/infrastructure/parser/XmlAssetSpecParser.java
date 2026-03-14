package ru.lifegame.assets.infrastructure.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.lifegame.assets.domain.model.asset.*;
import ru.lifegame.assets.domain.model.asset.AtlasConfigSchema.SingleCondition;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses unified visual-specs.xml files into AssetSpec domain objects.
 * Supports inheritance via extends="abstract/entities/human@1.0" attribute.
 * When extends is present, delegates to VisualSpecResolver for merging.
 */
public class XmlAssetSpecParser {

    private static final Logger log = LoggerFactory.getLogger(XmlAssetSpecParser.class);

    private VisualSpecResolver resolver;

    public XmlAssetSpecParser() {
        this.resolver = null;
    }

    /** Primary constructor: source-agnostic. */
    public XmlAssetSpecParser(SpecsSource source) {
        this.resolver = new VisualSpecResolver(source, this);
    }

    /** Backward-compatible constructor for disk-based specs. */
    public XmlAssetSpecParser(Path specsRoot) {
        this(new DiskSpecsSource(specsRoot, new XmlAssetSpecParser()));
    }

    public AssetSpec parse(Path xmlFile) {
        try (InputStream is = Files.newInputStream(xmlFile)) {
            return parseFromStream(is);
        } catch (XmlParseException e) {
            throw e;
        } catch (Exception e) {
            throw new XmlParseException("Failed to parse XML: " + xmlFile, e);
        }
    }

    public AssetSpec parseFromStream(InputStream inputStream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            doc.getDocumentElement().normalize();
            return parseRoot(doc.getDocumentElement(), null);
        } catch (XmlParseException e) {
            throw e;
        } catch (Exception e) {
            throw new XmlParseException("Failed to parse XML from stream", e);
        }
    }

    public Map<String, String> extractColorVarsFromFile(Path xmlFile) {
        try (InputStream is = Files.newInputStream(xmlFile)) {
            return extractColorVarsFromStream(is);
        } catch (Exception e) {
            log.warn("Failed to extract color vars from {}: {}", xmlFile, e.getMessage());
            return Map.of();
        }
    }

    public Map<String, String> extractColorVarsFromStream(InputStream is) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();
            return parseColorVariableMap(doc.getDocumentElement());
        } catch (Exception e) {
            log.warn("Failed to extract color vars from stream: {}", e.getMessage());
            return Map.of();
        }
    }

    private AssetSpec parseRoot(Element root, Path sourceFile) {
        String extendsAttr = root.getAttribute("extends");
        if (!extendsAttr.isBlank() && resolver != null) {
            return parseWithInheritance(root, extendsAttr, sourceFile);
        }
        return parseFlatSpec(root);
    }

    private AssetSpec parseWithInheritance(Element root, String extendsRef, Path sourceFile) {
        Element meta = getFirstChild(root, "meta");
        String entityType = getTextContent(meta, "entity-type");
        String entityName = getTextContent(meta, "entity-name");
        String version = getTextContentOrDefault(meta, "version", "1.0.0");

        Map<String, String> colorOverrides = parseColorVariableOverrides(root);
        List<ColorRemap> colorRemaps = parseColorRemaps(root);
        List<LayerOverride> layerOverrides = parseLayerOverrides(root);
        List<AnimationSpec> extraAnimations = parseAnimationsExtra(root);
        List<AssetLayer> ownLayers = parseLayersOptional(root);

        NamingSpec naming = parseNaming(root, entityType, entityName);
        AssetConstraints constraints = parseConstraints(root);

        Map<String, String> childColorVars = parseColorVariableMap(root);

        AssetSpec parentSpec = resolver.loadParent(extendsRef);

        List<AssetLayer> mergedLayers = resolver.mergeLayers(
                parentSpec.layers(), layerOverrides, ownLayers, colorOverrides, colorRemaps);

        Map<String, String> allColorVars = new HashMap<>(resolver.getParentColorVars(extendsRef));
        allColorVars.putAll(childColorVars);

        if (!allColorVars.isEmpty()) {
            mergedLayers = VisualSpecResolver.resolveColorVariablesInLayers(mergedLayers, allColorVars);
            log.debug("Resolved {} color variables in merged layers for {}/{}",
                    allColorVars.size(), entityType, entityName);
        }

        List<AnimationSpec> mergedAnimations = resolver.mergeAnimations(
                parentSpec.animations(), extraAnimations);

        ColorPalette mergedPalette = resolver.mergePalette(
                parentSpec.colorPalette(), parseColorPalette(root));

        log.info("Resolved inheritance for {}/{}: parent={}, layers={}, animations={}",
                entityType, entityName, extendsRef,
                mergedLayers.size(), mergedAnimations.size());

        return new AssetSpec(entityType, entityName, version,
                mergedLayers, mergedPalette, mergedAnimations,
                naming, constraints);
    }

    AssetSpec parseFlatSpec(Element root) {
        Element meta = getFirstChild(root, "meta");
        String entityType = getTextContent(meta, "entity-type");
        String entityName = getTextContent(meta, "entity-name");
        String version = getTextContentOrDefault(meta, "version", "1.0.0");

        List<AssetLayer> layers = parseLayers(root);
        Map<String, String> colorVars = parseColorVariableMap(root);
        if (!colorVars.isEmpty()) {
            layers = VisualSpecResolver.resolveColorVariablesInLayers(layers, colorVars);
        }
        ColorPalette palette = parseColorPalette(root);
        List<AnimationSpec> animations = parseAnimations(root);
        NamingSpec naming = parseNaming(root, entityType, entityName);
        AssetConstraints constraints = parseConstraints(root);

        return new AssetSpec(entityType, entityName, version,
                layers, palette, animations, naming, constraints);
    }

    private Map<String, String> parseColorVariableOverrides(Element root) {
        return parseColorVariableMap(root);
    }

    Map<String, String> parseColorVariableMap(Element root) {
        Map<String, String> vars = new HashMap<>();
        Element paletteEl = getFirstChildOrNull(root, "color-palette");
        if (paletteEl == null) return vars;
        NodeList colorNodes = paletteEl.getElementsByTagName("color");
        for (int i = 0; i < colorNodes.getLength(); i++) {
            Element el = (Element) colorNodes.item(i);
            String value = el.getAttribute("value");
            if (value.isBlank()) continue;
            String id = el.getAttribute("id");
            if (!id.isBlank()) vars.put("$" + id, value);
            String name = el.getAttribute("name");
            if (!name.isBlank()) {
                if (!name.startsWith("$")) name = "$" + name;
                vars.put(name, value);
            }
        }
        return vars;
    }

    private List<ColorRemap> parseColorRemaps(Element root) {
        List<ColorRemap> remaps = new ArrayList<>();
        Element overridesEl = getFirstChildOrNull(root, "layer-overrides");
        if (overridesEl == null) return remaps;
        NodeList remapNodes = overridesEl.getElementsByTagName("color-remap");
        for (int i = 0; i < remapNodes.getLength(); i++) {
            Element el = (Element) remapNodes.item(i);
            String from = el.getAttribute("from");
            String to = el.getAttribute("to");
            if (!from.isBlank() && !to.isBlank()) remaps.add(new ColorRemap(from, to));
        }
        return remaps;
    }

    private List<LayerOverride> parseLayerOverrides(Element root) {
        List<LayerOverride> overrides = new ArrayList<>();
        Element overridesEl = getFirstChildOrNull(root, "layer-overrides");
        if (overridesEl == null) return overrides;
        NodeList layerNodes = overridesEl.getElementsByTagName("layer");
        for (int i = 0; i < layerNodes.getLength(); i++) {
            Element el = (Element) layerNodes.item(i);
            String id = el.getAttribute("id");
            boolean replace = "true".equals(el.getAttribute("replace"));
            if (id.isBlank()) continue;
            PixelData pixelData = parsePixelData(el);
            String type = el.getAttribute("type");
            int zOrder = intAttr(el, "z-order", -1);
            int width = intAttr(el, "width", 0);
            int height = intAttr(el, "height", 0);
            String follows = el.hasAttribute("follows") ? el.getAttribute("follows") : null;
            List<LayerCondition> conditions = parseLayerConditions(el);
            overrides.add(new LayerOverride(id, replace, type, zOrder, width, height,
                    pixelData, follows, conditions));
        }
        return overrides;
    }

    private List<AnimationSpec> parseAnimationsExtra(Element root) {
        List<AnimationSpec> animations = new ArrayList<>();
        Element animsEl = getFirstChildOrNull(root, "animations-extra");
        if (animsEl == null) return animations;
        NodeList animNodes = animsEl.getElementsByTagName("animation");
        for (int i = 0; i < animNodes.getLength(); i++) {
            animations.add(parseOneAnimation((Element) animNodes.item(i)));
        }
        return animations;
    }

    private List<AssetLayer> parseLayersOptional(Element root) {
        Element layersEl = getFirstChildOrNull(root, "layers");
        if (layersEl == null) return List.of();
        return parseLayerElements(layersEl);
    }

    private List<AssetLayer> parseLayers(Element root) {
        Element layersEl = getFirstChildOrNull(root, "layers");
        if (layersEl == null) throw new XmlParseException("Missing required <layers> element");
        return parseLayerElements(layersEl);
    }

    private List<AssetLayer> parseLayerElements(Element layersEl) {
        List<AssetLayer> layers = new ArrayList<>();
        NodeList layerNodes = layersEl.getElementsByTagName("layer");
        for (int i = 0; i < layerNodes.getLength(); i++) {
            Element el = (Element) layerNodes.item(i);
            String id = el.getAttribute("id");
            if (id.isBlank()) id = el.getAttribute("type");
            String type = el.getAttribute("type");
            int zOrder = intAttr(el, "z-order", i);
            int width = intAttr(el, "width", 0);
            int height = intAttr(el, "height", 0);
            PixelData pixelData = parsePixelData(el);
            String follows = el.hasAttribute("follows") ? el.getAttribute("follows") : null;
            List<LayerCondition> conditions = parseLayerConditions(el);
            layers.add(new AssetLayer(id, type, "", zOrder, width, height,
                    pixelData, follows, conditions));
        }
        return layers;
    }

    private List<LayerCondition> parseLayerConditions(Element layerEl) {
        List<LayerCondition> conditions = new ArrayList<>();
        Element conditionsEl = getFirstChildOrNull(layerEl, "conditions");
        if (conditionsEl == null) return conditions;
        NodeList condNodes = conditionsEl.getElementsByTagName("condition");
        for (int i = 0; i < condNodes.getLength(); i++) {
            Element condEl = (Element) condNodes.item(i);
            String condId = condEl.getAttribute("id");
            if (condId.isBlank()) continue;
            Element overrideEl = getFirstChildOrNull(condEl, "override");
            if (overrideEl == null) continue;
            String layerRef = overrideEl.getAttribute("layer-ref");
            String tint = overrideEl.getAttribute("tint");
            String opacity = overrideEl.getAttribute("opacity");
            conditions.add(new LayerCondition(condId, tint, opacity, layerRef));
        }
        return conditions;
    }

    PixelData parsePixelData(Element layerEl) {
        Element pdEl = getFirstChildOrNull(layerEl, "pixel-data");
        if (pdEl == null) return PixelData.EMPTY;

        List<PixelRect> rects = new ArrayList<>();
        List<PixelLine> lines = new ArrayList<>();
        List<PixelDot> dots = new ArrayList<>();

        NodeList children = pdEl.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (!(children.item(i) instanceof Element child)) continue;
            switch (child.getTagName()) {
                case "rect" -> rects.add(new PixelRect(
                        intAttr(child, "x", 0), intAttr(child, "y", 0),
                        intAttr(child, "w", 1), intAttr(child, "h", 1),
                        child.getAttribute("color")));
                case "pixel" -> dots.add(new PixelDot(
                        intAttr(child, "x", 0), intAttr(child, "y", 0),
                        child.getAttribute("color")));
                case "hline" -> lines.add(new PixelLine(
                        intAttr(child, "x", 0), intAttr(child, "y", 0),
                        intAttr(child, "len", 1),
                        PixelLine.Direction.HORIZONTAL,
                        child.getAttribute("color")));
                case "vline" -> lines.add(new PixelLine(
                        intAttr(child, "x", 0), intAttr(child, "y", 0),
                        intAttr(child, "len", 1),
                        PixelLine.Direction.VERTICAL,
                        child.getAttribute("color")));
                default -> log.warn("Unknown pixel-data element: {}", child.getTagName());
            }
        }
        return new PixelData(rects, lines, dots);
    }

    private List<AnimationSpec> parseAnimations(Element root) {
        List<AnimationSpec> animations = new ArrayList<>();
        Element animsEl = getFirstChildOrNull(root, "animations");
        if (animsEl == null) return animations;
        NodeList animNodes = animsEl.getElementsByTagName("animation");
        for (int i = 0; i < animNodes.getLength(); i++) {
            animations.add(parseOneAnimation((Element) animNodes.item(i)));
        }
        return animations;
    }

    private AnimationSpec parseOneAnimation(Element el) {
        String name = el.getAttribute("name");
        int frames = intAttr(el, "frames", 24);
        int fps = intAttr(el, "fps", 12);
        boolean loop = boolAttr(el, "loop", true);
        int frameWidth = intAttr(el, "frame-width", 128);
        int frameHeight = intAttr(el, "frame-height", 128);

        List<FrameOffset> frameOffsets = parseFrameOffsets(el);
        List<AnimationVariant> variants = parseVariants(el, fps, loop, frameOffsets);

        return new AnimationSpec(name, frames, fps, loop, frameWidth, frameHeight,
                frameOffsets, variants);
    }

    private List<AnimationVariant> parseVariants(Element animEl, int defaultFps,
                                                 boolean defaultLoop,
                                                 List<FrameOffset> defaultOffsets) {
        List<AnimationVariant> variants = new ArrayList<>();
        NodeList children = animEl.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (!(node instanceof Element child)) continue;
            if (!"variant".equals(child.getTagName())) continue;

            int varFps = intAttr(child, "fps", defaultFps);
            boolean varLoop = boolAttr(child, "loop", defaultLoop);
            List<SingleCondition> conditions = parseSingleConditions(child);
            variants.add(new AnimationVariant(conditions, varFps, varLoop, defaultOffsets));
        }
        return variants;
    }

    private List<SingleCondition> parseSingleConditions(Element variantEl) {
        List<SingleCondition> conditions = new ArrayList<>();
        NodeList children = variantEl.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (!(node instanceof Element child)) continue;
            if (!"condition".equals(child.getTagName())) continue;

            String space = child.getAttribute("space");
            String npcId = child.hasAttribute("npcId") ? child.getAttribute("npcId") : null;
            String stat = child.getAttribute("stat");
            String operator = child.getAttribute("operator");
            String rawValue = child.getAttribute("value");

            Object value;
            try {
                value = Long.parseLong(rawValue);
            } catch (NumberFormatException e) {
                value = rawValue;
            }

            if (!space.isBlank() && !stat.isBlank() && !operator.isBlank()) {
                conditions.add(new SingleCondition(space, npcId, stat, operator, value));
            }
        }
        return conditions;
    }

    private List<FrameOffset> parseFrameOffsets(Element animEl) {
        List<FrameOffset> offsets = new ArrayList<>();
        NodeList children = animEl.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (!(node instanceof Element child)) continue;
            if (!"frame".equals(child.getTagName())) continue;
            int index = intAttr(child, "index", offsets.size());
            String offsetsStr = child.getAttribute("layer-offsets");
            Map<String, int[]> layerOffsets = parseLayerOffsets(offsetsStr);
            offsets.add(new FrameOffset(index, layerOffsets));
        }
        return offsets;
    }

    private Map<String, int[]> parseLayerOffsets(String s) {
        Map<String, int[]> map = new HashMap<>();
        if (s == null || s.isBlank()) return map;
        String[] entries = s.split(";");
        for (String entry : entries) {
            entry = entry.trim();
            if (entry.isEmpty()) continue;
            String[] parts = entry.split(":");
            if (parts.length != 2) continue;
            String layerId = parts[0].trim();
            String[] coords = parts[1].trim().split(",");
            if (coords.length == 2) {
                try {
                    int dx = Integer.parseInt(coords[0].trim());
                    int dy = Integer.parseInt(coords[1].trim());
                    map.put(layerId, new int[]{dx, dy});
                } catch (NumberFormatException ignored) {}
            }
        }
        return map;
    }

    private ColorPalette parseColorPalette(Element root) {
        Element paletteEl = getFirstChildOrNull(root, "color-palette");
        if (paletteEl == null) return ColorPalette.projectDefault();
        List<String> primary = parseColorList(paletteEl, "primary");
        List<String> secondary = parseColorList(paletteEl, "secondary");
        if (primary.isEmpty() && secondary.isEmpty()) return ColorPalette.projectDefault();
        return new ColorPalette(primary, secondary);
    }

    private List<String> parseColorList(Element parent, String tagName) {
        List<String> colors = new ArrayList<>();
        Element el = getFirstChildOrNull(parent, tagName);
        if (el == null) return colors;
        NodeList colorNodes = el.getElementsByTagName("color");
        for (int i = 0; i < colorNodes.getLength(); i++) {
            String hex = ((Element) colorNodes.item(i)).getAttribute("hex");
            if (!hex.isBlank()) colors.add(hex);
        }
        return colors;
    }

    private NamingSpec parseNaming(Element root, String entityType, String entityName) {
        Element namingEl = getFirstChildOrNull(root, "naming");
        if (namingEl == null) return new NamingSpec(entityType, entityName, null);
        String outputDir = getTextContentOrDefault(namingEl, "output-dir",
                "assets/" + entityType + "/" + entityName);
        return new NamingSpec(entityType, entityName, outputDir);
    }

    private AssetConstraints parseConstraints(Element root) {
        Element el = getFirstChildOrNull(root, "constraints");
        if (el == null) return AssetConstraints.defaults();
        return new AssetConstraints(
                intChild(el, "max-file-size-kb", 500),
                intChild(el, "max-sprite-sheet-width", 2048),
                intChild(el, "max-sprite-sheet-height", 2048),
                intChild(el, "compression-quality", 85),
                intChild(el, "bit-depth", 32));
    }

    Element getFirstChild(Element parent, String tag) {
        Element el = getFirstChildOrNull(parent, tag);
        if (el == null) throw new XmlParseException("Missing required element: <" + tag + ">");
        return el;
    }

    Element getFirstChildOrNull(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        return nodes.getLength() > 0 ? (Element) nodes.item(0) : null;
    }

    private String getTextContent(Element parent, String tag) {
        return getFirstChild(parent, tag).getTextContent().trim();
    }

    String getTextContentOrDefault(Element parent, String tag, String def) {
        Element el = getFirstChildOrNull(parent, tag);
        if (el == null) return def;
        String text = el.getTextContent().trim();
        return text.isEmpty() ? def : text;
    }

    int intAttr(Element el, String attr, int def) {
        String val = el.getAttribute(attr);
        if (val.isBlank()) return def;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private boolean boolAttr(Element el, String attr, boolean def) {
        String val = el.getAttribute(attr);
        return val.isBlank() ? def : Boolean.parseBoolean(val);
    }

    private int intChild(Element parent, String tag, int def) {
        Element el = getFirstChildOrNull(parent, tag);
        if (el == null) return def;
        try {
            return Integer.parseInt(el.getTextContent().trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
