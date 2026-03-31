package ru.lifegame.assets.domain.model.asset;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Binds a named behaviour (e.g. {@code "blink"}, {@code "breathe"}) to
 * a specific layer within an asset spec.
 *
 * <p>An asset can declare any number of bindings; the runtime behaviour
 * engine resolves them by {@code behaviorId} without knowing concrete
 * layer names in advance (java-developer-skill.md §5.2).
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code layerId}    — the layer this binding targets (matches
 *       {@link AssetLayer#id()})</li>
 *   <li>{@code behaviorId} — identifies the behaviour class/script;
 *       never an enum value — always a plain string token</li>
 *   <li>{@code params}     — optional key-value overrides forwarded to
 *       the behaviour engine; immutable copy at construction</li>
 * </ul>
 *
 * Ref: java-developer-skill.md §5.2. TASK-BE-017.
 */
public record LayerBinding(
        String layerId,
        String behaviorId,
        Map<String, String> params
) {
    public LayerBinding {
        Objects.requireNonNull(layerId,    "layerId must not be null");
        Objects.requireNonNull(behaviorId, "behaviorId must not be null");
        if (layerId.isBlank())    throw new IllegalArgumentException("layerId must not be blank");
        if (behaviorId.isBlank()) throw new IllegalArgumentException("behaviorId must not be blank");
        params = params != null ? Collections.unmodifiableMap(params) : Map.of();
    }

    /** Convenience constructor: binding without extra params. */
    public LayerBinding(String layerId, String behaviorId) {
        this(layerId, behaviorId, Map.of());
    }
}
