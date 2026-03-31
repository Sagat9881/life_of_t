package ru.lifegame.backend.infrastructure.spec;

import ru.lifegame.backend.domain.narrative.spec.BlockManifest;
import ru.lifegame.backend.domain.narrative.spec.SpecEntry;
import ru.lifegame.backend.domain.narrative.spec.SpecLoadException;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a {@code manifest.xml} file into a {@link BlockManifest}.
 *
 * <p>Expected XML format:
 * <pre>{@code
 * <manifest block-id="quest" version="1.0">
 *   <entry entity-id="morning_routine" entity-type="quest"
 *          spec-path="narrative/quest/morning_routine/spec.xml"/>
 *   <entry entity-id="wedding_prep"   entity-type="quest"
 *          spec-path="narrative/quest/wedding_prep/spec.xml"/>
 * </manifest>
 * }</pre>
 *
 * <p>Infrastructure concern — XML/DOM stays out of domain.
 * Ref: java-developer-skill.md §7, §5.5. TASK-BE-016.
 */
public class XmlManifestParser {

    /**
     * Parses {@code manifest.xml} from the given stream.
     *
     * @param xmlStream  input stream of the manifest file; caller owns closing
     * @param sourceName human-readable name used in error messages
     * @return parsed {@link BlockManifest}
     * @throws SpecLoadException if XML is malformed or required attributes are absent
     */
    public BlockManifest parse(InputStream xmlStream, String sourceName) {
        Document doc;
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(false);
            doc = f.newDocumentBuilder().parse(xmlStream);
        } catch (Exception e) {
            throw new SpecLoadException(sourceName, "Failed to parse manifest XML: " + sourceName, e);
        }
        return parseDocument(doc, sourceName);
    }

    // ── internals ─────────────────────────────────────────────────────────────

    private BlockManifest parseDocument(Document doc, String sourceName) {
        Element root = doc.getDocumentElement();

        String blockId = requireAttr(root, "block-id", sourceName);
        String version = root.getAttribute("version");
        if (version.isBlank()) version = "1.0";

        List<SpecEntry> entries = new ArrayList<>();
        NodeList nodes = root.getElementsByTagName("entry");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String entityId   = requireAttr(el, "entity-id",   sourceName);
            String entityType = requireAttr(el, "entity-type", sourceName);
            String specPath   = requireAttr(el, "spec-path",   sourceName);
            entries.add(new SpecEntry(entityId, entityType, specPath));
        }
        return new BlockManifest(blockId, version, entries);
    }

    private String requireAttr(Element el, String attr, String sourceName) {
        String val = el.getAttribute(attr);
        if (val == null || val.isBlank())
            throw new SpecLoadException(sourceName,
                    "Missing required attribute '" + attr + "' in manifest: " + sourceName);
        return val;
    }
}
