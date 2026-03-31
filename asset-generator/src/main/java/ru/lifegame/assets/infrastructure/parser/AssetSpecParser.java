package ru.lifegame.assets.infrastructure.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.lifegame.assets.domain.model.asset.*;
import ru.lifegame.assets.infrastructure.scanner.EntityRef;
import ru.lifegame.assets.infrastructure.scanner.SpecsManifestReader;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Manifest-driven facade for parsing {@link AssetSpec} from XML.
 *
 * <h3>Two usage patterns</h3>
 * <ol>
 *   <li><b>Single-spec</b> ({@link #parse(Path)} / {@link #parse(InputStream, String)})
 *       — used by tests and direct callers that already hold a file path.</li>
 *   <li><b>Manifest-driven bulk load</b> ({@link #loadAll(SpecsSource)})
 *       — reads {@code specs-manifest.xml} via {@link SpecsManifestReader}, then
 *       parses each non-abstract entity path; no directory traversal ever occurs
 *       (java-developer-skill.md §5.2, §5.5).</li>
 * </ol>
 *
 * <h3>Design constraints (java-developer-skill.md §5.2)</h3>
 * <ul>
 *   <li>No {@code switch}/{@code if} on animation keys, layer ids, or entity names.</li>
 *   <li>No {@code enum} with concrete asset identifiers.</li>
 *   <li>Adding a new entity spec requires only a new XML file + manifest entry;
 *       zero code changes.</li>
 * </ul>
 *
 * <p>Internally delegates raw XML-to-{@link AssetSpec} parsing to
 * {@link XmlAssetSpecParser}; this class adds the manifest orchestration layer.
 *
 * <p>Ref: java-developer-skill.md §3.2, §5.2, §5.5. TASK-BE-016, TASK-BE-017.
 */
public class AssetSpecParser {

    private static final Logger log = LoggerFactory.getLogger(AssetSpecParser.class);

    private final XmlAssetSpecParser xmlParser;

    /** Production constructor — uses the standard XML parser. */
    public AssetSpecParser() {
        this.xmlParser = new XmlAssetSpecParser();
    }

    /** Test/DI constructor — inject a custom XML parser stub. */
    public AssetSpecParser(XmlAssetSpecParser xmlParser) {
        this.xmlParser = Objects.requireNonNull(xmlParser, "xmlParser must not be null");
    }

    // ── single-spec API ──────────────────────────────────────────────────────

    /**
     * Parses a single {@link AssetSpec} from a filesystem path.
     *
     * @param xmlPath absolute or relative path to the spec XML file
     * @return parsed spec, including {@code inheritsFrom} and {@code bindings}
     * @throws XmlParseException if file cannot be read or XML is malformed
     */
    public AssetSpec parse(Path xmlPath) {
        Objects.requireNonNull(xmlPath, "xmlPath must not be null");
        try (InputStream is = Files.newInputStream(xmlPath)) {
            return parse(is, xmlPath.toString());
        } catch (XmlParseException e) {
            throw e;
        } catch (IOException e) {
            throw new XmlParseException("Cannot open spec file: " + xmlPath, e);
        }
    }

    /**
     * Parses a single {@link AssetSpec} from an already-opened stream.
     *
     * @param stream     input stream of the spec XML; caller owns closing
     * @param sourceName human-readable name for error messages
     * @return parsed spec
     */
    public AssetSpec parse(InputStream stream, String sourceName) {
        Objects.requireNonNull(stream,     "stream must not be null");
        Objects.requireNonNull(sourceName, "sourceName must not be null");
        Document doc = parseXml(stream, sourceName);
        AssetSpec base = xmlParser.parseDocument(doc, sourceName);
        return enrichWithExtensions(doc.getDocumentElement(), base, sourceName);
    }

    // ── manifest-driven bulk API ─────────────────────────────────────────────

    /**
     * Loads all non-abstract asset specs declared in {@code specs-manifest.xml}
     * within the given {@link SpecsSource}.
     *
     * <p>Abstract entities (marked {@code abstract="true"} in the manifest) are
     * skipped — they serve as inheritance bases only and should not be generated.
     *
     * <p>No directory traversal occurs; the manifest is the sole source of truth
     * about which entities exist (java-developer-skill.md §5.5).
     *
     * @param source classpath or disk source for spec files
     * @return immutable list of all loadable asset specs; order follows manifest
     * @throws XmlParseException if {@code specs-manifest.xml} or any spec is unreadable
     */
    public List<AssetSpec> loadAll(SpecsSource source) {
        Objects.requireNonNull(source, "source must not be null");
        SpecsManifestReader reader = new SpecsManifestReader(source);
        List<EntityRef> refs = reader.readManifest();

        List<AssetSpec> result = new ArrayList<>(refs.size());
        for (EntityRef ref : refs) {
            if (ref.isAbstract()) {
                log.debug("Skipping abstract entity: {}", ref.path());
                continue;
            }
            try (InputStream is = source.openSpec(ref.path())) {
                AssetSpec spec = parse(is, ref.path());
                result.add(spec);
                log.debug("Loaded asset spec: {} / {}", spec.entityType(), spec.entityName());
            } catch (XmlParseException e) {
                throw e;
            } catch (IOException e) {
                throw new XmlParseException(
                        "Cannot open spec at path '" + ref.path() + "' from manifest", e);
            }
        }
        log.info("AssetSpecParser.loadAll: loaded {} specs from manifest", result.size());
        return Collections.unmodifiableList(result);
    }

    // ── extension parsing (inheritsFrom + bindings) ───────────────────────────

    /**
     * Reads TASK-BE-017 extension elements ({@code <inheritsFrom>}, {@code <bindings>})
     * from the root element and merges them into the base {@link AssetSpec} parsed by
     * {@link XmlAssetSpecParser}.
     *
     * <p>If neither element is present the original spec is returned unchanged.
     */
    private AssetSpec enrichWithExtensions(Element root, AssetSpec base, String sourceName) {
        Optional<AssetId> inheritsFrom = parseInheritsFrom(root, sourceName);
        List<LayerBinding> bindings = parseBindings(root, sourceName);

        if (inheritsFrom.isEmpty() && bindings.isEmpty()) {
            // Nothing to add — return the original spec without copying
            // (backward-compatible 8-arg constructor sets the fields to their defaults)
            return base;
        }

        // Rebuild with extensions; all other fields passed through unchanged.
        return new AssetSpec(
                base.entityType(),
                base.entityName(),
                base.version(),
                base.layers(),
                base.colorPalette(),
                base.animations(),
                base.naming(),
                base.constraints(),
                inheritsFrom,
                bindings
        );
    }

    private Optional<AssetId> parseInheritsFrom(Element root, String sourceName) {
        NodeList nodes = root.getElementsByTagName("inheritsFrom");
        if (nodes.getLength() == 0) return Optional.empty();
        Element el = (Element) nodes.item(0);
        String ref = el.getTextContent().trim();
        if (ref.isBlank()) return Optional.empty();
        try {
            return Optional.of(AssetId.parse(ref));
        } catch (IllegalArgumentException e) {
            throw new XmlParseException(
                    "Invalid <inheritsFrom> value '" + ref + "' in " + sourceName
                    + ": expected 'entityType/entityName' format");
        }
    }

    private List<LayerBinding> parseBindings(Element root, String sourceName) {
        NodeList bindingsNodes = root.getElementsByTagName("bindings");
        if (bindingsNodes.getLength() == 0) return List.of();
        Element bindingsEl = (Element) bindingsNodes.item(0);

        List<LayerBinding> result = new ArrayList<>();
        NodeList bindNodes = bindingsEl.getElementsByTagName("bind");
        for (int i = 0; i < bindNodes.getLength(); i++) {
            Element bind = (Element) bindNodes.item(i);
            String layerId    = requireAttr(bind, "layer",    sourceName);
            String behaviorId = requireAttr(bind, "behavior", sourceName);

            Map<String, String> params = new LinkedHashMap<>();
            NodeList paramNodes = bind.getElementsByTagName("param");
            for (int j = 0; j < paramNodes.getLength(); j++) {
                Element param = (Element) paramNodes.item(j);
                String key = requireAttr(param, "name",  sourceName);
                String val = requireAttr(param, "value", sourceName);
                params.put(key, val);
            }
            result.add(new LayerBinding(layerId, behaviorId, params));
        }
        return result;
    }

    private String requireAttr(Element el, String attr, String sourceName) {
        String val = el.getAttribute(attr);
        if (val == null || val.isBlank())
            throw new XmlParseException(
                    "Missing required attribute '" + attr + "' on <" + el.getTagName()
                    + "> in " + sourceName);
        return val;
    }

    private Document parseXml(InputStream stream, String sourceName) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setNamespaceAware(false);
            return f.newDocumentBuilder().parse(stream);
        } catch (Exception e) {
            throw new XmlParseException("Failed to parse XML from: " + sourceName, e);
        }
    }
}
