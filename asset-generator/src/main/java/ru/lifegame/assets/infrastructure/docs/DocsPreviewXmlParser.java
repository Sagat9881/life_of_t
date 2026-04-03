package ru.lifegame.assets.infrastructure.docs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.lifegame.assets.domain.model.docs.ColorEntry;
import ru.lifegame.assets.domain.model.docs.ConstraintsDescriptor;
import ru.lifegame.assets.domain.model.docs.EntityDocsDescriptor;
import ru.lifegame.assets.infrastructure.parser.XmlParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight XML parser dedicated to the docs-preview pipeline.
 *
 * <p>Reads only the fields required for {@link EntityDocsDescriptor} (FR-2):
 * {@code entityType}, {@code entityName}, animations list, color-palette,
 * constraints, and idle sprite-atlas file name.
 *
 * <p>Does NOT reuse {@link ru.lifegame.assets.infrastructure.parser.XmlAssetSpecParser}
 * to avoid pulling in the full asset-generation dependency graph.
 *
 * <p>Design constraints:
 * <ul>
 *   <li>No hardcoded entity identifiers — all data is read from XML attributes.</li>
 *   <li>No switch/if on entity names or types.</li>
 * </ul>
 *
 * <p>Ref: TASK-BE-DOC-001, visual-docs-preview-mode.md FR-2.
 */
public class DocsPreviewXmlParser {

    private static final Logger log = LoggerFactory.getLogger(DocsPreviewXmlParser.class);

    /**
     * Parses one entity's {@code visual-specs.xml} from the given stream.
     *
     * @param is          open input stream of the XML file; caller owns closing
     * @param manifestPath path from the manifest (e.g. {@code "characters/tanya"}),
     *                     used to derive {@code id} and {@code type}
     * @return populated {@link EntityDocsDescriptor}
     * @throws XmlParseException on XML parsing failure
     */
    public EntityDocsDescriptor parse(InputStream is, String manifestPath) {
        Document doc = parseXml(is, manifestPath);
        Element root = doc.getDocumentElement();

        // --- meta ---
        Element meta = getFirstChildOrNull(root, "meta");
        String entityName = meta != null
                ? textContentOrDefault(meta, "entity-name", EntityDocsDescriptor.idFromPath(manifestPath))
                : EntityDocsDescriptor.idFromPath(manifestPath);

        String id          = EntityDocsDescriptor.idFromPath(manifestPath);
        String type        = EntityDocsDescriptor.typeFromPath(manifestPath);
        String displayName = EntityDocsDescriptor.displayName(entityName);

        // --- idle sprite atlas file name ---
        String spriteAtlasFile = parseSpriteAtlasFile(root, entityName);

        // --- animations ---
        List<String> animations = parseAnimationNames(root);

        // --- color palette ---
        List<ColorEntry> colorPalette = parseColorPalette(root);

        // --- constraints ---
        ConstraintsDescriptor constraints = parseConstraints(root);

        return new EntityDocsDescriptor(
                id, manifestPath, type, displayName,
                spriteAtlasFile, animations, colorPalette,
                constraints, false
        );
    }

    // ── idle sprite atlas ────────────────────────────────────────────────────

    /**
     * Derives the idle sprite-atlas PNG file name from the first animation named
     * "idle" (case-insensitive), or falls back to {@code <entityName>_idle.png}
     * convention. Returns {@code null} if no animations are defined at all.
     */
    private String parseSpriteAtlasFile(Element root, String entityName) {
        Element animsEl = getFirstChildOrNull(root, "animations");
        if (animsEl == null) {
            animsEl = getFirstChildOrNull(root, "animations-extra");
        }
        if (animsEl == null) return null;

        NodeList animNodes = animsEl.getElementsByTagName("animation");
        if (animNodes.getLength() == 0) return null;

        // First, look for an "idle" animation
        for (int i = 0; i < animNodes.getLength(); i++) {
            Element anim = (Element) animNodes.item(i);
            String name = anim.getAttribute("name");
            if ("idle".equalsIgnoreCase(name)) {
                return entityName + "_idle.png";
            }
        }
        // Fallback: use the first animation's name
        String firstName = ((Element) animNodes.item(0)).getAttribute("name");
        return entityName + "_" + firstName + ".png";
    }

    // ── animations ───────────────────────────────────────────────────────────

    /**
     * Collects animation names from {@code <animations>} and
     * {@code <animations-extra>} (deduplicated, manifest order).
     */
    private List<String> parseAnimationNames(Element root) {
        List<String> names = new ArrayList<>();
        collectAnimationNames(root, "animations", names);
        collectAnimationNames(root, "animations-extra", names);
        return names;
    }

    private void collectAnimationNames(Element root, String tag, List<String> names) {
        Element animsEl = getFirstChildOrNull(root, tag);
        if (animsEl == null) return;
        NodeList animNodes = animsEl.getElementsByTagName("animation");
        for (int i = 0; i < animNodes.getLength(); i++) {
            String name = ((Element) animNodes.item(i)).getAttribute("name");
            if (!name.isBlank() && !names.contains(name)) {
                names.add(name);
            }
        }
    }

    // ── color palette ─────────────────────────────────────────────────────────

    /**
     * Parses {@code <color-palette>/<color>} entries into
     * {@link ColorEntry} list. Supports both {@code id} and {@code name}
     * attributes for the symbolic name.
     */
    private List<ColorEntry> parseColorPalette(Element root) {
        List<ColorEntry> entries = new ArrayList<>();
        Element paletteEl = getFirstChildOrNull(root, "color-palette");
        if (paletteEl == null) return entries;

        NodeList colorNodes = paletteEl.getElementsByTagName("color");
        for (int i = 0; i < colorNodes.getLength(); i++) {
            Element el = (Element) colorNodes.item(i);
            String hex = el.getAttribute("value");
            if (hex.isBlank()) hex = el.getAttribute("hex");
            if (hex.isBlank()) continue;

            // Prefer id, fallback to name
            String name = el.getAttribute("id");
            if (name.isBlank()) name = el.getAttribute("name");
            if (name.isBlank()) name = hex; // last resort: use the hex itself

            if (!name.startsWith("$")) name = "$" + name;
            entries.add(new ColorEntry(name, hex));
        }
        return entries;
    }

    // ── constraints ──────────────────────────────────────────────────────────

    private ConstraintsDescriptor parseConstraints(Element root) {
        Element el = getFirstChildOrNull(root, "constraints");
        if (el == null) return null;

        Integer maxColors = intChildOrNull(el, "max-colors");
        Integer pixelSize = intChildOrNull(el, "pixel-size");
        boolean antiAliasing = Boolean.parseBoolean(
                textContentOrDefault(el, "anti-aliasing", "false"));

        if (maxColors == null && pixelSize == null) return null;
        return new ConstraintsDescriptor(maxColors, pixelSize, antiAliasing);
    }

    // ── XML helpers ───────────────────────────────────────────────────────────

    private Document parseXml(InputStream is, String sourceName) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            throw new XmlParseException("docs-preview: failed to parse XML from '" + sourceName + "'", e);
        }
    }

    private Element getFirstChildOrNull(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        return nodes.getLength() > 0 ? (Element) nodes.item(0) : null;
    }

    private String textContentOrDefault(Element parent, String tag, String def) {
        Element el = getFirstChildOrNull(parent, tag);
        if (el == null) return def;
        String text = el.getTextContent().trim();
        return text.isEmpty() ? def : text;
    }

    private Integer intChildOrNull(Element parent, String tag) {
        Element el = getFirstChildOrNull(parent, tag);
        if (el == null) return null;
        try {
            return Integer.parseInt(el.getTextContent().trim());
        } catch (NumberFormatException e) {
            log.warn("docs-preview: non-integer value in <{}>", tag);
            return null;
        }
    }
}
