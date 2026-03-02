package ru.lifegame.lpc.compositor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Loads layer configuration from JSON metadata
 */
@Slf4j
public class LayerConfigLoader {
    
    private final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * Load layer metadata from resources
     */
    public Map<String, Object> loadLayerMetadata(String resourcePath) {
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream(resourcePath)) {
            if (is == null) {
                log.warn("Layer metadata not found: {}", resourcePath);
                return Map.of();
            }
            return mapper.readValue(is, Map.class);
        } catch (IOException e) {
            log.error("Failed to load layer metadata: {}", resourcePath, e);
            return Map.of();
        }
    }
}