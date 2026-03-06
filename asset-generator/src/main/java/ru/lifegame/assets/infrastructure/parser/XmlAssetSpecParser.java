package ru.lifegame.assets.infrastructure.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ru.lifegame.assets.domain.model.asset.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlAssetSpecParser {

    private static final Logger log = LoggerFactory.getLogger(XmlAssetSpecParser.class);

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
            return parseRoot(doc.getDocumentElement());
        } catch (XmlParseException e) {
            throw e;
        } catch (Exception e) {
            throw new XmlParseException("Failed to parse XML from stream", e);
        }
    }

    private AssetSpec parseRoot(Element root) {
        Element meta = getFirstChild(root, "meta");
        String entityType = getTextContent(meta, "entity-type");
        String entityName = getTextContent(meta, "entity-name");
        String version = getTextContentOrDefault(meta, "version", "1.0.0");

        List<AssetLayer> layers = parseLayers(root);
        ColorPalette palette = parseColorPalette(root);
        List<AnimationSpec> animations = parseAnimations(root);
        List<TimeOfDayVariation> variations = parseTimeOfDayVariations(root);
        NamingSpec naming = parseNaming(root, entityType, entityName);
        AssetConstraints constraints = parseConstraints(root);

        return new AssetSpec(entityType, entityName, version,
                layers, palette, animations, variations, naming, constraints);
    }

    private List<AssetLayer> parseLayers(Element root) {
        List<AssetLayer> layers = new ArrayList<>();
        Element layersEl = getFirstChildOrNull(root, "layers");
        if (layersEl == null) {
            throw new XmlParseException("Missing required <layers> element");
        }
        NodeList layerNodes = layersEl.getElementsByTagName("layer");
        for (int i = 0; i < layerNodes.getLength(); i++) {
            Element el = (Element) layerNodes.item(i);
            String id = el.getAttribute("id");
            if (id.isBlank()) id = el.getAttribute("type");
            String type = el.getAttribute("type");
            int zOrder = intAttr(el, "z-order", i);
            int width = intAttr(el, "width", 0);
            int height = intAttr(el, "height", 0);
            String description = "";

            PixelData pixelData = parsePixelData(el);
            List<LayerCondition> conditions = parseLayerConditions(el);

            layers.add(new AssetLayer(id, type, description, zOrder, width, height, pixelData, conditions));
        }
        return layers;
    }

    /**
     * Parses {@code <conditions>} block within a layer.
     * Each {@code <condition>} has an id and an {@code <override>} with tint and opacity.
     * <pre>
     * &lt;conditions&gt;
     *   &lt;condition id="time_morning"&gt;
     *     &lt;override layer-ref="ambient_light" tint="#E8F4FF" opacity="0.12"/&gt;
     *   &lt;/condition&gt;
     * &lt;/conditions&gt;
     * </pre>
     */
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

    private PixelData parsePixelData(Element layerEl) {
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
            Element el = (Element) animNodes.item(i);
            String name = el.getAttribute("name");
            int frames = intAttr(el, "frames", 24);
            int fps = intAttr(el, "fps", 12);
            boolean loop = boolAttr(el, "loop", true);
            int frameWidth = intAttr(el, "frame-width", 128);
            int frameHeight = intAttr(el, "frame-height", 128);

            List<FrameOffset> frameOffsets = parseFrameOffsets(el);
            animations.add(new AnimationSpec(name, frames, fps, loop,
                    frameWidth, frameHeight, frameOffsets));
        }
        return animations;
    }

    private List<FrameOffset> parseFrameOffsets(Element animEl) {
        List<FrameOffset> offsets = new ArrayList<>();
        NodeList frameNodes = animEl.getElementsByTagName("frame");
        for (int i = 0; i < frameNodes.getLength(); i++) {
            Element el = (Element) frameNodes.item(i);
            int index = intAttr(el, "index", i);
            String offsetsStr = el.getAttribute("layer-offsets");
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

    private List<TimeOfDayVariation> parseTimeOfDayVariations(Element root) {
        List<TimeOfDayVariation> variations = new ArrayList<>();
        Element todsEl = getFirstChildOrNull(root, "time-of-day-variations");
        if (todsEl == null) return variations;
        NodeList varNodes = todsEl.getElementsByTagName("variation");
        for (int i = 0; i < varNodes.getLength(); i++) {
            Element el = (Element) varNodes.item(i);
            String time = el.getAttribute("time");
            String lighting = getTextContentOrDefault(el, "lighting", "");
            String mood = getTextContentOrDefault(el, "mood", "");
            variations.add(new TimeOfDayVariation(time, lighting, mood));
        }
        return variations;
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

    // ---- XML helpers ----

    private Element getFirstChild(Element parent, String tag) {
        Element el = getFirstChildOrNull(parent, tag);
        if (el == null) throw new XmlParseException("Missing required element: <" + tag + ">");
        return el;
    }

    private Element getFirstChildOrNull(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        return nodes.getLength() > 0 ? (Element) nodes.item(0) : null;
    }

    private String getTextContent(Element parent, String tag) {
        return getFirstChild(parent, tag).getTextContent().trim();
    }

    private String getTextContentOrDefault(Element parent, String tag, String def) {
        Element el = getFirstChildOrNull(parent, tag);
        if (el == null) return def;
        String text = el.getTextContent().trim();
        return text.isEmpty() ? def : text;
    }

    private int intAttr(Element el, String attr, int def) {
        String val = el.getAttribute(attr);
        if (val.isBlank()) return def;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return def; }
    }

    private boolean boolAttr(Element el, String attr, boolean def) {
        String val = el.getAttribute(attr);
        return val.isBlank() ? def : Boolean.parseBoolean(val);
    }

    private int intChild(Element parent, String tag, int def) {
        Element el = getFirstChildOrNull(parent, tag);
        if (el == null) return def;
        try { return Integer.parseInt(el.getTextContent().trim()); } catch (NumberFormatException e) { return def; }
    }
}
