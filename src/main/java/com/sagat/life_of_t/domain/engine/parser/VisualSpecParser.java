package com.sagat.life_of_t.domain.engine.parser;

import com.sagat.life_of_t.domain.engine.spec.VisualSpec;
import com.sagat.life_of_t.domain.engine.spec.VisualSpec.*;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

/**
 * Parses a single visual-specs.xml into a raw VisualSpec.
 * Does NOT resolve inheritance — use {@link VisualSpecResolver} for that.
 */
public class VisualSpecParser {

    public VisualSpec parse(InputStream xml) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(xml);
        Element root = doc.getDocumentElement();

        boolean isAbstract = Boolean.parseBoolean(
                root.getAttribute("abstract"));
        String version = root.getAttribute("version");
        String extendsRef = root.hasAttribute("extends")
                ? root.getAttribute("extends") : null;

        Element meta = firstChild(root, "meta");
        String entityName = textOf(meta, "entity-name");
        String entityType = textOf(meta, "entity-type");
        String specVersion = textOfOrDefault(meta, "spec-version", version);

        CanvasSpec canvas = parseCanvas(meta);
        double displayScale = parseDisplayScale(meta);
        Map<String, String> palette = parsePalette(root);
        List<LayerSpec> layers = parseLayers(root);
        List<LayerSpec> overrides = parseLayerOverrides(root);
        List<LayerSpec> allLayers = new ArrayList<>(layers);
        allLayers.addAll(overrides);

        List<AnimationSpec> animations = parseAnimations(root, "animations");
        List<AnimationSpec> extras = parseAnimations(root, "animations-extra");
        List<AnimationSpec> allAnims = new ArrayList<>(animations);
        allAnims.addAll(extras);

        return new VisualSpec(
                entityName, entityType, specVersion, isAbstract,
                extendsRef, canvas, displayScale,
                Collections.unmodifiableMap(palette),
                Collections.unmodifiableList(allLayers),
                Collections.unmodifiableList(allAnims)
        );
    }

    private CanvasSpec parseCanvas(Element meta) {
        NodeList canvasList = meta.getElementsByTagName("canvas");
        if (canvasList.getLength() == 0) return null;
        Element c = (Element) canvasList.item(0);
        return new CanvasSpec(
                Integer.parseInt(c.getAttribute("width")),
                Integer.parseInt(c.getAttribute("height"))
        );
    }

    private double parseDisplayScale(Element meta) {
        NodeList nodes = meta.getElementsByTagName("display-scale");
        if (nodes.getLength() == 0) return 1.0;
        return Double.parseDouble(nodes.item(0).getTextContent().trim());
    }

    private Map<String, String> parsePalette(Element root) {
        Map<String, String> palette = new LinkedHashMap<>();
        NodeList paletteNodes = root.getElementsByTagName("color-palette");
        if (paletteNodes.getLength() == 0) return palette;

        Element paletteEl = (Element) paletteNodes.item(0);
        NodeList colors = paletteEl.getElementsByTagName("color");
        for (int i = 0; i < colors.getLength(); i++) {
            Element c = (Element) colors.item(i);
            palette.put(c.getAttribute("id"), c.getAttribute("value"));
        }
        return palette;
    }

    private List<LayerSpec> parseLayers(Element root) {
        List<LayerSpec> layers = new ArrayList<>();
        NodeList layerNodes = root.getElementsByTagName("layers");
        if (layerNodes.getLength() == 0) return layers;

        Element layersEl = (Element) layerNodes.item(0);
        if (!"layers".equals(layersEl.getTagName())) return layers;

        NodeList children = layersEl.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (!(children.item(i) instanceof Element el)) continue;
            if (!"layer".equals(el.getTagName())) continue;
            layers.add(parseLayer(el, false));
        }
        return layers;
    }

    private List<LayerSpec> parseLayerOverrides(Element root) {
        List<LayerSpec> overrides = new ArrayList<>();
        NodeList overrideNodes = root.getElementsByTagName("layer-overrides");
        if (overrideNodes.getLength() == 0) return overrides;

        Element overridesEl = (Element) overrideNodes.item(0);
        NodeList children = overridesEl.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (!(children.item(i) instanceof Element el)) continue;
            if (!"layer".equals(el.getTagName())) continue;
            boolean replace = Boolean.parseBoolean(
                    el.hasAttribute("replace") ? el.getAttribute("replace") : "true");
            overrides.add(parseLayer(el, replace));
        }
        return overrides;
    }

    private LayerSpec parseLayer(Element el, boolean replace) {
        String id = el.getAttribute("id");
        String type = el.hasAttribute("type") ? el.getAttribute("type") : "base";
        int zOrder = el.hasAttribute("z-order")
                ? Integer.parseInt(el.getAttribute("z-order")) : 0;
        int width = el.hasAttribute("width")
                ? Integer.parseInt(el.getAttribute("width")) : 0;
        int height = el.hasAttribute("height")
                ? Integer.parseInt(el.getAttribute("height")) : 0;

        List<PixelPrimitive> pixelData = parsePixelData(el);
        return new LayerSpec(id, type, zOrder, width, height, replace, pixelData);
    }

    private List<PixelPrimitive> parsePixelData(Element layerEl) {
        List<PixelPrimitive> primitives = new ArrayList<>();
        NodeList dataNodes = layerEl.getElementsByTagName("pixel-data");
        if (dataNodes.getLength() == 0) return primitives;

        Element pixelDataEl = (Element) dataNodes.item(0);
        NodeList children = pixelDataEl.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (!(children.item(i) instanceof Element el)) continue;
            switch (el.getTagName()) {
                case "rect" -> primitives.add(new PixelRect(
                        intAttr(el, "x"), intAttr(el, "y"),
                        intAttr(el, "w"), intAttr(el, "h"),
                        el.getAttribute("color")));
                case "hline" -> primitives.add(new PixelLine(
                        intAttr(el, "x"), intAttr(el, "y"),
                        intAttr(el, "len"),
                        el.getAttribute("color")));
                case "pixel" -> primitives.add(new PixelDot(
                        intAttr(el, "x"), intAttr(el, "y"),
                        el.getAttribute("color")));
            }
        }
        return primitives;
    }

    private List<AnimationSpec> parseAnimations(Element root, String containerTag) {
        List<AnimationSpec> anims = new ArrayList<>();
        NodeList containers = root.getElementsByTagName(containerTag);
        if (containers.getLength() == 0) return anims;

        Element container = (Element) containers.item(0);
        NodeList animNodes = container.getElementsByTagName("animation");
        for (int i = 0; i < animNodes.getLength(); i++) {
            Element anim = (Element) animNodes.item(i);
            if (!containerTag.equals(anim.getParentNode().getNodeName())) continue;

            String name = anim.getAttribute("name");
            int frames = intAttr(anim, "frames");
            int fps = intAttr(anim, "fps");
            boolean loop = Boolean.parseBoolean(anim.getAttribute("loop"));
            int fw = intAttr(anim, "frame-width");
            int fh = intAttr(anim, "frame-height");

            List<FrameSpec> frameSpecs = parseFrames(anim);
            anims.add(new AnimationSpec(name, frames, fps, loop, fw, fh, frameSpecs));
        }
        return anims;
    }

    private List<FrameSpec> parseFrames(Element animEl) {
        List<FrameSpec> frames = new ArrayList<>();
        NodeList frameNodes = animEl.getElementsByTagName("frame");
        for (int i = 0; i < frameNodes.getLength(); i++) {
            Element frame = (Element) frameNodes.item(i);
            if (!"frame".equals(frame.getTagName())) continue;
            if (!animEl.equals(frame.getParentNode())) continue;

            int index = intAttr(frame, "index");
            Map<String, LayerOffset> offsets = parseLayerOffsets(
                    frame.getAttribute("layer-offsets"));
            frames.add(new FrameSpec(index, Collections.unmodifiableMap(offsets)));
        }
        return frames;
    }

    private Map<String, LayerOffset> parseLayerOffsets(String raw) {
        Map<String, LayerOffset> offsets = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) return offsets;

        for (String part : raw.split(";")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            String[] kv = trimmed.split(":");
            if (kv.length != 2) continue;
            String layerId = kv[0].trim();
            String[] coords = kv[1].trim().split(",");
            if (coords.length != 2) continue;
            offsets.put(layerId, new LayerOffset(
                    Integer.parseInt(coords[0].trim()),
                    Integer.parseInt(coords[1].trim())));
        }
        return offsets;
    }

    private Element firstChild(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        return list.getLength() > 0 ? (Element) list.item(0) : null;
    }

    private String textOf(Element parent, String tag) {
        Element el = firstChild(parent, tag);
        return el != null ? el.getTextContent().trim() : "";
    }

    private String textOfOrDefault(Element parent, String tag, String fallback) {
        String val = textOf(parent, tag);
        return val.isEmpty() ? fallback : val;
    }

    private int intAttr(Element el, String name) {
        String val = el.getAttribute(name);
        return val.isEmpty() ? 0 : Integer.parseInt(val);
    }
}
