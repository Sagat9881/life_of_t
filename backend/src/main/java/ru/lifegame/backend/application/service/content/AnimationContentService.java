package ru.lifegame.backend.application.service.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

/**
 * Loads animation metadata from generated sprite-atlas.json files.
 *
 * <p>At startup scans all {@code classpath:/assets/**/sprite-atlas.json} files
 * produced by the asset generator and builds a per-entity animation catalogue.
 * The result is cached in-memory — data is static for the lifetime of the JVM.
 *
 * <p>Atlas JSON schema (configVersion 1.4):
 * <pre>
 * {
 *   "configVersion": "1.4",
 *   "entity": "persi",
 *   "displayScale": 1.5,
 *   "animations": {
 *     "idle": { "file": "idle_atlas.png", "layout": "strip",
 *               "fps": 4, "loop": true, "columns": 16,
 *               "frameWidth": 128, "frameHeight": 96 },
 *     ...
 *   }
 * }
 * </pre>
 */
@Service
public class AnimationContentService {

    private static final Logger log = LoggerFactory.getLogger(AnimationContentService.class);
    private static final String ATLAS_PATTERN = "classpath*:assets/**/sprite-atlas.json";

    private final ObjectMapper mapper = new ObjectMapper();

    /** entityId -> list of animation descriptors */
    private Map<String, List<Map<String, Object>>> cache = Collections.emptyMap();

    @PostConstruct
    public void init() {
        cache = loadAll();
        log.info("AnimationContentService: loaded {} entities from sprite-atlas.json files",
                cache.size());
    }

    /**
     * Returns all animation definitions keyed by entity id.
     * Guaranteed non-null; each value list is unmodifiable.
     */
    public Map<String, List<Map<String, Object>>> getAllAnimations() {
        return cache;
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private Map<String, List<Map<String, Object>>> loadAll() {
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();

        try {
            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(ATLAS_PATTERN);

            if (resources.length == 0) {
                log.warn("No sprite-atlas.json found on classpath (pattern: {}). "
                        + "Run 'mvn generate-resources' first.", ATLAS_PATTERN);
                return result;
            }

            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    JsonNode root = mapper.readTree(is);
                    String entityId = extractEntityId(root, resource);
                    List<Map<String, Object>> anims = parseAnimations(root, entityId);
                    result.put(entityId, Collections.unmodifiableList(anims));
                    log.debug("  Loaded {} animations for entity '{}' from {}",
                            anims.size(), entityId, resource.getFilename());
                } catch (Exception e) {
                    log.error("Failed to parse sprite-atlas.json from {}: {}",
                            resource.getDescription(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error scanning for sprite-atlas.json files: {}", e.getMessage(), e);
        }

        return result;
    }

    /**
     * Resolves entity id: prefer the "entity" field in JSON,
     * fall back to parent directory name from the resource URL.
     */
    private String extractEntityId(JsonNode root, Resource resource) {
        JsonNode entityNode = root.get("entity");
        if (entityNode != null && !entityNode.isNull() && !entityNode.asText().isBlank()) {
            return entityNode.asText();
        }
        // Fallback: derive from URL path (e.g. .../assets/characters/persi/sprite-atlas.json)
        try {
            String url = resource.getURL().getPath();
            String[] parts = url.split("/");
            // sprite-atlas.json is the last part, entity dir is second-to-last
            if (parts.length >= 2) {
                return parts[parts.length - 2];
            }
        } catch (Exception ignored) {}
        return "unknown";
    }

    /**
     * Parses the "animations" object from the atlas JSON.
     * Each entry becomes: {name, file, layout, fps, loop, columns, frameWidth, frameHeight,
     *                       renderMode?, rows?, displayScale?}
     */
    private List<Map<String, Object>> parseAnimations(JsonNode root, String entityId) {
        List<Map<String, Object>> result = new ArrayList<>();

        JsonNode animsNode = root.get("animations");
        if (animsNode == null || !animsNode.isObject()) {
            log.warn("No 'animations' object in atlas for entity '{}'", entityId);
            return result;
        }

        double displayScale = root.has("displayScale") ? root.get("displayScale").asDouble(1.0) : 1.0;
        String configVersion = root.has("configVersion") ? root.get("configVersion").asText("?") : "?";

        Iterator<Map.Entry<String, JsonNode>> fields = animsNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String animName = entry.getKey();
            JsonNode animNode = entry.getValue();

            Map<String, Object> anim = new LinkedHashMap<>();
            anim.put("name", animName);
            anim.put("file", getTextOrNull(animNode, "file"));
            anim.put("layout", getTextOrNull(animNode, "layout"));
            anim.put("renderMode", getTextOrNull(animNode, "renderMode"));
            anim.put("fps", animNode.has("fps") ? animNode.get("fps").asInt() : 0);
            anim.put("loop", animNode.has("loop") && animNode.get("loop").asBoolean(true));
            anim.put("columns", animNode.has("columns") ? animNode.get("columns").asInt() : 0);
            anim.put("frameWidth", animNode.has("frameWidth") ? animNode.get("frameWidth").asInt() : 0);
            anim.put("frameHeight", animNode.has("frameHeight") ? animNode.get("frameHeight").asInt() : 0);
            anim.put("displayScale", displayScale);
            anim.put("configVersion", configVersion);

            // Grid layout: include rows for multi-row atlases (e.g. location overlays)
            if (animNode.has("rows")) {
                anim.put("rows", mapper.convertValue(animNode.get("rows"), List.class));
            }
            // CropOffset — pass through for frontend renderer to handle
            if (animNode.has("cropOffset")) {
                anim.put("cropOffset", mapper.convertValue(animNode.get("cropOffset"), Map.class));
            }

            result.add(anim);
        }

        return result;
    }

    private String getTextOrNull(JsonNode node, String field) {
        return node.has(field) ? node.get(field).asText(null) : null;
    }
}
