package ru.lifegame.assets.infrastructure.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.lifegame.assets.infrastructure.parser.SpecsSource;
import ru.lifegame.assets.infrastructure.parser.XmlParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads specs-manifest.xml from the given {@link SpecsSource} and returns
 * an ordered list of {@link EntityRef} entries.
 *
 * <p>This is the classpath-safe alternative to {@link PromptDirectoryScanner}:
 * it does not rely on {@code Files.walk} and works inside nested JARs.
 * {@link PromptDirectoryScanner} is retained as a disk-mode fallback.
 */
public class SpecsManifestReader {

    private static final Logger log = LoggerFactory.getLogger(SpecsManifestReader.class);
    private static final String MANIFEST_PATH = "specs-manifest.xml";

    private final SpecsSource source;

    public SpecsManifestReader(SpecsSource source) {
        this.source = source;
    }

    /**
     * Reads and parses {@code specs-manifest.xml} from the specs source.
     *
     * @return ordered list of entity refs; never null
     * @throws XmlParseException if the manifest cannot be opened or parsed
     */
    public List<EntityRef> readManifest() {
        if (!source.specExists(MANIFEST_PATH)) {
            throw new XmlParseException(
                    "specs-manifest.xml not found in specs source — cannot enumerate entities");
        }

        try (InputStream is = source.openSpec(MANIFEST_PATH)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();
            return parseManifest(doc.getDocumentElement());
        } catch (XmlParseException e) {
            throw e;
        } catch (Exception e) {
            throw new XmlParseException("Failed to parse " + MANIFEST_PATH, e);
        }
    }

    private List<EntityRef> parseManifest(Element root) {
        List<EntityRef> refs = new ArrayList<>();
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (!(node instanceof Element el)) continue;
            if (!"entity".equals(el.getTagName())) continue;

            String path = el.getAttribute("path");
            if (path == null || path.isBlank()) {
                log.warn("specs-manifest.xml: <entity> without path attribute — skipping");
                continue;
            }
            boolean isAbstract = "true".equalsIgnoreCase(el.getAttribute("abstract"));
            refs.add(new EntityRef(path, isAbstract));
        }
        log.info("specs-manifest.xml: loaded {} entity refs ({} abstract)",
                refs.size(), refs.stream().filter(EntityRef::isAbstract).count());
        return refs;
    }
}
