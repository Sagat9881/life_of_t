package ru.lifegame.assets;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable request object describing what asset to generate.
 *
 * @param type   the kind of asset being requested
 * @param name   logical name for this asset (e.g. "wood_floor", "tatyana_idle")
 * @param width  desired output image width in pixels
 * @param height desired output image height in pixels
 * @param params generator-specific string parameters (e.g. colors, seed, symmetry)
 */
public record AssetRequest(
        AssetType type,
        String name,
        int width,
        int height,
        Map<String, String> params
) {
    public AssetRequest {
        if (type == null)   throw new IllegalArgumentException("type must not be null");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (width  <= 0)    throw new IllegalArgumentException("width must be positive");
        if (height <= 0)    throw new IllegalArgumentException("height must be positive");
        params = params != null ? Collections.unmodifiableMap(params) : Collections.emptyMap();
    }

    /** Convenience factory — no extra params. */
    public static AssetRequest of(AssetType type, String name, int width, int height) {
        return new AssetRequest(type, name, width, height, Collections.emptyMap());
    }

    /** Returns the string param value or the supplied default. */
    public String param(String key, String defaultValue) {
        return params.getOrDefault(key, defaultValue);
    }
}
