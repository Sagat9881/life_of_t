package ru.lifegame.assets.infrastructure.docs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.assets.domain.model.docs.ColorEntry;
import ru.lifegame.assets.domain.model.docs.DocsPreviewResult;
import ru.lifegame.assets.domain.model.docs.EntityDocsDescriptor;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Infrastructure adapter: serialises a {@link DocsPreviewResult} to
 * {@code docs-preview.json} in the configured output directory.
 *
 * <p>Output format matches FR-2 from {@code visual-docs-preview-mode.md}:
 * <pre>{@code
 * [
 *   {
 *     "id": "tanya",
 *     "path": "characters/tanya",
 *     "type": "characters",
 *     "displayName": "Tanya",
 *     "spriteAtlasFile": "tanya_idle.png",
 *     "animations": ["idle", "walk", "run"],
 *     "colorPalette": [ {"name": "$skin_base", "hex": "#F0C098"} ],
 *     "constraints": { "maxColors": 32, "pixelSize": 1, "antiAliasing": false },
 *     "abstractEntity": false
 *   }
 * ]
 * }</pre>
 *
 * <p>Uses the Jackson {@link ObjectMapper} already on the classpath
 * (asset-generator depends on jackson-databind for the atlas writer).
 *
 * <p>Ref: TASK-BE-DOC-001, ADR-001.
 */
public class DocsPreviewJsonWriterAdapter {

    private static final Logger log = LoggerFactory.getLogger(DocsPreviewJsonWriterAdapter.class);
    private static final String OUTPUT_FILE_NAME = "docs-preview.json";

    private final ObjectMapper mapper;

    /** Production constructor. */
    public DocsPreviewJsonWriterAdapter() {
        this.mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    /** Test/DI constructor. */
    public DocsPreviewJsonWriterAdapter(ObjectMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    /**
     * Writes the result as {@code docs-preview.json} into {@code outputDir}.
     *
     * @param result    the use-case result to serialise
     * @param outputDir target directory; created if absent
     * @return path to the written file
     * @throws IOException if the file cannot be written
     */
    public Path write(DocsPreviewResult result, Path outputDir) throws IOException {
        Objects.requireNonNull(result,    "result must not be null");
        Objects.requireNonNull(outputDir, "outputDir must not be null");

        Files.createDirectories(outputDir);
        Path target = outputDir.resolve(OUTPUT_FILE_NAME);

        ArrayNode root = mapper.createArrayNode();
        for (EntityDocsDescriptor d : result.descriptors()) {
            root.add(toJson(d));
        }

        try (OutputStream os = Files.newOutputStream(target)) {
            mapper.writeValue(os, root);
        }

        log.info("docs-preview.json written: {} entities → {}", result.descriptors().size(), target);
        return target;
    }

    // ── serialisation helpers ────────────────────────────────────────────────

    private ObjectNode toJson(EntityDocsDescriptor d) {
        ObjectNode node = mapper.createObjectNode();
        node.put("id",             d.id());
        node.put("path",           d.path());
        node.put("type",           d.type());
        node.put("displayName",    d.displayName());
        node.put("spriteAtlasFile",d.spriteAtlasFile());

        ArrayNode animations = node.putArray("animations");
        d.animations().forEach(animations::add);

        ArrayNode palette = node.putArray("colorPalette");
        for (ColorEntry ce : d.colorPalette()) {
            ObjectNode entry = mapper.createObjectNode();
            entry.put("name", ce.name());
            entry.put("hex",  ce.hex());
            palette.add(entry);
        }

        if (d.constraints() != null) {
            ObjectNode constraints = node.putObject("constraints");
            if (d.constraints().maxColors() != null)
                constraints.put("maxColors", d.constraints().maxColors());
            if (d.constraints().pixelSize() != null)
                constraints.put("pixelSize", d.constraints().pixelSize());
            constraints.put("antiAliasing", d.constraints().antiAliasing());
        } else {
            node.putNull("constraints");
        }

        node.put("abstractEntity", d.abstractEntity());
        return node;
    }
}
