package ru.lifegame.assets.infrastructure.parser;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ru.lifegame.assets.domain.model.asset.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a {@code visual-specs.xml} file into an {@link AssetSpec} domain
 * object.
 *
 * <p>The parser is intentionally lenient: optional elements that are absent
 * from the XML result in empty collections or {@code null} field values,
 * never in a parse exception.  Only structurally invalid XML or a missing
 * mandatory {@code <asset>} root element will throw
 * {@link XmlParseException}.</p>
 *
 * <h2>Expected document structure</h2>
 * <pre>{@code
 * <asset id="..." category="...">
 *   <description>...</description>
 *
 *   <layers>
 *     <layer name="..." type="..." paletteRef="..." zIndex="0" optional="false"/>
 *   </layers>
 *
 *   <animations>
 *     <animation state="..." frameCount="4" fps="8" loop="true">
 *       <frames>0 1 2 3</frames>
 *     </animation>
 *   </animations>
 *
 *   <palettes>
 *     <palette name="...">
 *       <color>#RRGGBB</color>
 *     </palette>
 *   </palettes>
 *
 *   <variations>
 *     <variation timeOfDay="..." colorShift="#RRGGBB" opacity="0.2"/>
 *   </variations>
 *
 *   <naming prefix="..." layerPattern="..." atlasPattern="..." configPattern="..."/>
 *
 *   <constraints widthPx="64" heightPx="64" maxFrames="16"
 *                allowTransparency="true" outputFormats="png,webp"/>
 * </asset>
 * }</pre>
 */
@Component
public class XmlAssetSpecParser {

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Parse the XML file at {@code specPath} into an {@link AssetSpec}.
     *
     * @param specPath path to the {@code visual-specs.xml} file
     * @return fully populated {@link AssetSpec}
     * @throws XmlParseException if the file cannot be read or parsed
     */
    public AssetSpec parse(Path specPath) {
        Document doc = loadDocument(specPath);
        Element root = doc.getDocumentElement();

        if (!"asset".equals(root.getTagName())) {
            throw new XmlParseException("Root element must be <asset>, found: <" + root.getTagName() + ">");
        }

        String id          = mandatory(root, "id",       specPath);
        String category    = mandatory(root, "category", specPath);
        String description = textOf(firstChild(root, "description"));

        List<AssetLayer>        layers     = parseLayers(root);
        List<AnimationSpec>     animations = parseAnimations(root);
        List<ColorPalette>      palettes   = parsePalettes(root);
        List<TimeOfDayVariation> variations = parseVariations(root);
        NamingSpec              naming     = parseNaming(root);
        AssetConstraints        constraints = parseConstraints(root);

        return new AssetSpec(id, category, description,
                             layers, animations, palettes, variations,
                             naming, constraints);
    }

    // -----------------------------------------------------------------------
    // Document loading
    // -----------------------------------------------------------------------

    private Document loadDocument(Path path) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(path.toFile());
        } catch (Exception e) {
            throw new XmlParseException("Failed to load XML from " + path + ": " + e.getMessage(), e);
        }
    }

    // -----------------------------------------------------------------------
    // Section parsers
    // -----------------------------------------------------------------------

    private List<AssetLayer> parseLayers(Element root) {
        Element layersEl = firstChild(root, "layers");
        if (layersEl == null) return List.of();

        List<AssetLayer> result = new ArrayList<>();
        NodeList nodes = layersEl.getElementsByTagName("layer");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            result.add(new AssetLayer(
                    attr(el, "name"),
                    attr(el, "type"),
                    el.getAttribute("paletteRef"),          // optional
                    intAttr(el, "zIndex", 0),
                    boolAttr(el, "optional", false)
            ));
        }
        return result;
    }

    private List<AnimationSpec> parseAnimations(Element root) {
        Element animsEl = firstChild(root, "animations");
        if (animsEl == null) return List.of();

        List<AnimationSpec> result = new ArrayList<>();
        NodeList nodes = animsEl.getElementsByTagName("animation");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            List<Integer> frames = parseFrames(firstChild(el, "frames"));
            result.add(new AnimationSpec(
                    attr(el, "state"),
                    intAttr(el, "frameCount", frames.size()),
                    intAttr(el, "fps", 8),
                    boolAttr(el, "loop", true),
                    frames
            ));
        }
        return result;
    }

    private List<Integer> parseFrames(Element framesEl) {
        if (framesEl == null) return List.of();
        String text = framesEl.getTextContent().trim();
        if (text.isEmpty()) return List.of();
        List<Integer> result = new ArrayList<>();
        for (String token : text.split("\\s+")) {
            result.add(Integer.parseInt(token));
        }
        return result;
    }

    private List<ColorPalette> parsePalettes(Element root) {
        Element palettesEl = firstChild(root, "palettes");
        if (palettesEl == null) return List.of();

        List<ColorPalette> result = new ArrayList<>();
        NodeList nodes = palettesEl.getElementsByTagName("palette");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el     = (Element) nodes.item(i);
            String  name   = attr(el, "name");
            List<String> colors = new ArrayList<>();
            NodeList colorNodes = el.getElementsByTagName("color");
            for (int j = 0; j < colorNodes.getLength(); j++) {
                colors.add(colorNodes.item(j).getTextContent().trim());
            }
            result.add(new ColorPalette(name, colors));
        }
        return result;
    }

    private List<TimeOfDayVariation> parseVariations(Element root) {
        Element varsEl = firstChild(root, "variations");
        if (varsEl == null) return List.of();

        List<TimeOfDayVariation> result = new ArrayList<>();
        NodeList nodes = varsEl.getElementsByTagName("variation");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            result.add(new TimeOfDayVariation(
                    attr(el, "timeOfDay"),
                    attr(el, "colorShift"),
                    doubleAttr(el, "opacity", 0.0)
            ));
        }
        return result;
    }

    private NamingSpec parseNaming(Element root) {
        Element el = firstChild(root, "naming");
        if (el == null) return null;
        return new NamingSpec(
                attr(el, "prefix"),
                attr(el, "layerPattern"),
                attr(el, "atlasPattern"),
                attr(el, "configPattern")
        );
    }

    private AssetConstraints parseConstraints(Element root) {
        Element el = firstChild(root, "constraints");
        if (el == null) return null;
        return new AssetConstraints(
                intAttr(el, "widthPx",  64),
                intAttr(el, "heightPx", 64),
                intAttr(el, "maxFrames", 16),
                boolAttr(el, "allowTransparency", true),
                el.getAttribute("outputFormats")
        );
    }

    // -----------------------------------------------------------------------
    // XML helpers
    // -----------------------------------------------------------------------

    private Element firstChild(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        return list.getLength() == 0 ? null : (Element) list.item(0);
    }

    private String textOf(Element el) {
        return el == null ? "" : el.getTextContent().trim();
    }

    private String attr(Element el, String name) {
        return el.getAttribute(name);
    }

    private String mandatory(Element root, String attr, Path path) {
        String val = root.getAttribute(attr);
        if (val == null || val.isBlank())
            throw new XmlParseException("Missing mandatory attribute '" + attr + "' in " + path);
        return val;
    }

    private int intAttr(Element el, String name, int defaultValue) {
        String val = el.getAttribute(name);
        return (val == null || val.isBlank()) ? defaultValue : Integer.parseInt(val);
    }

    private double doubleAttr(Element el, String name, double defaultValue) {
        String val = el.getAttribute(name);
        return (val == null || val.isBlank()) ? defaultValue : Double.parseDouble(val);
    }

    private boolean boolAttr(Element el, String name, boolean defaultValue) {
        String val = el.getAttribute(name);
        return (val == null || val.isBlank()) ? defaultValue : Boolean.parseBoolean(val);
    }
}
