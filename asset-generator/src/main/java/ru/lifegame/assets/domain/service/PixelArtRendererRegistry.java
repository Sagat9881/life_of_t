package ru.lifegame.assets.domain.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry of all available {@link PixelArtRenderer} implementations.
 * Thread-safe after construction (immutable snapshot).
 */
public final class PixelArtRendererRegistry {

    private final Map<String, PixelArtRenderer> renderers;

    public PixelArtRendererRegistry(List<PixelArtRenderer> rendererList) {
        Map<String, PixelArtRenderer> map = new LinkedHashMap<>();
        for (PixelArtRenderer r : rendererList) {
            map.put(r.spriteId(), r);
        }
        this.renderers = Collections.unmodifiableMap(map);
    }

    /**
     * Looks up a renderer by sprite id.
     */
    public Optional<PixelArtRenderer> find(String spriteId) {
        return Optional.ofNullable(renderers.get(spriteId));
    }

    /**
     * Returns all registered renderer ids.
     */
    public List<String> allIds() {
        return List.copyOf(renderers.keySet());
    }
}
