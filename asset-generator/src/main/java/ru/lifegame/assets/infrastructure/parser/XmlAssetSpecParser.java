package ru.lifegame.assets.infrastructure.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ru.lifegame.assets.domain.model.asset.AnimationSpec;
import ru.lifegame.assets.domain.model.asset.AssetConstraints;
import ru.lifegame.assets.domain.model.asset.AssetLayer;
import ru.lifegame.assets.domain.model.asset.AssetSpec;
import ru.lifegame.assets.domain.model.asset.ColorPalette;
import ru.lifegame.assets.domain.model.asset.NamingSpec;
import ru.lifegame.assets.domain.model.asset.TimeOfDayVariation;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses unified XML asset specifications into {@link AssetSpec} domain objects.
 * <p>
 * Expected XML structure follows docs/prompts/_core/unified-asset-schema.xml.
 */
public class XmlAssetSpecParser {

    private static final Logger log = LoggerFactory.getLogger(XmlAssetSpecParser.class);

    /**
     * Parses an XML file at the given path into an {@link AssetSpec}.
     *
     * @param xmlFile path to the unified XML spec
     * @return parsed asset specification
     * @throws XmlParseException if parsing fails
     */
    public AssetSpec parse(Path xmlFile) {
        try (InputStream is = Files.newInputStream(xmlFile)) {
            return parseFromStream(is);
        } catch (XmlParseException e) {
            throw e;
        } catch (Exception e) {
            throw new XmlParseException("Failed to parse XML: " + xmlFile, e);
        }
    }

    /**
     * Parses an XML input stream into an {@link AssetSpec}.
     * Visible for testing.
     */
    public AssetSpec parseFromStream(InputStream inputStream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            return parseRoot(root);
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
            if (id.isBlank()) {
                id = el.getAttribute("type");
            }
            String type = el.getAttribute("type");
            String description = el.getTextContent().trim();
            int zOrder = i;
            String zOrderAttr = el.getAttribute("z-order");
            if (!zOrderAttr.isBlank()) {
                zOrder = Integer.parseInt(zOrderAttr);
            }
            layers.add(new AssetLayer(id, type, description, zOrder));
        }
        return layers;
    }

    private ColorPalette parseColorPalette(Element root) {
        Element paletteEl = getFirstChildOrNull(root, "color-palette");
        if (paletteEl == null) {
            return ColorPalette.projectDefault();
        }
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
            Element colorEl = (Element) colorNodes.item(i);
            String hex = colorEl.getAttribute("hex");
            if (!hex.isBlank()) {
                colors.add(hex);
            }
        }
        return colors;
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
            animations.add(new AnimationSpec(name, frames, fps, loop, frameWidth, frameHeight));
        }
        return animations;
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
        if (namingEl == null) {
            return new NamingSpec(entityType, entityName, null);
        }
        String outputDir = getTextContentOrDefault(namingEl, "output-dir",
                "assets/" + entityType + "/" + entityName);
        return new NamingSpec(entityType, entityName, outputDir);
    }

    private AssetConstraints parseConstraints(Element root) {
        Element constraintsEl = getFirstChildOrNull(root, "constraints");
        if (constraintsEl == null) {
            return AssetConstraints.defaults();
        }
        int maxFileSize = intChild(constraintsEl, "max-file-size-kb", 500);
        int maxWidth = intChild(constraintsEl, "max-sprite-sheet-width", 2048);
        int maxHeight = intChild(constraintsEl, "max-sprite-sheet-height", 2048);
        int quality = intChild(constraintsEl, "compression-quality", 85);
        int bitDepth = intChild(constraintsEl, "bit-depth", 32);
        return new AssetConstraints(maxFileSize, maxWidth, maxHeight, quality, bitDepth);
    }

    // ---- XML helper methods ----

    private Element getFirstChild(Element parent, String tagName) {
        Element el = getFirstChildOrNull(parent, tagName);
        if (el == null) {
            throw new XmlParseException("Missing required element: <" + tagName + ">");
        }
        return el;
    }

    private Element getFirstChildOrNull(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) return null;
        return (Element) nodes.item(0);
    }

    private String getTextContent(Element parent, String tagName) {
        Element el = getFirstChild(parent, tagName);
        return el.getTextContent().trim();
    }

    private String getTextContentOrDefault(Element parent, String tagName, String defaultValue) {
        Element el = getFirstChildOrNull(parent, tagName);
        if (el == null) return defaultValue;
        String text = el.getTextContent().trim();
        return text.isEmpty() ? defaultValue : text;
    }

    private int intAttr(Element el, String attr, int defaultValue) {
        String val = el.getAttribute(attr);
        if (val.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean boolAttr(Element el, String attr, boolean defaultValue) {
        String val = el.getAttribute(attr);
        if (val.isBlank()) return defaultValue;
        return Boolean.parseBoolean(val);
    }

    private int intChild(Element parent, String tagName, int defaultValue) {
        Element el = getFirstChildOrNull(parent, tagName);
        if (el == null) return defaultValue;
        try {
            return Integer.parseInt(el.getTextContent().trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
