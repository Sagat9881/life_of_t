package ru.lifegame.assets.domain.model.asset;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Complete specification for a layered asset, parsed from a unified XML file.
 * This is the central domain model consumed by the LayeredAssetGenerator.
 *
 * <p>Extended in TASK-BE-017 with:
 * <ul>
 *   <li>{@code inheritsFrom} — optional reference to a parent asset spec
 *       ({@link AssetId}); the generator merges parent layers/animations
 *       before applying overrides from this spec.</li>
 *   <li>{@code bindings} — zero or more {@link LayerBinding} entries that
 *       attach runtime behaviours (e.g. blink, breathe) to specific layers
 *       by id, without switch-casing on layer names (§5.2).</li>
 * </ul>
 *
 * <p>All previous constructors are preserved for backward compatibility.
 *
 * @param entityType    "characters", "locations", "pets"
 * @param entityName    snake_case name of the entity
 * @param version       spec version string
 * @param layers        ordered list of layers to generate
 * @param colorPalette  color palette to use
 * @param animations    list of animation specifications (may be empty)
 * @param naming        naming conventions
 * @param constraints   technical constraints
 * @param inheritsFrom  optional parent asset reference for spec inheritance
 * @param bindings      zero or more layer-behaviour bindings
 */
public record AssetSpec(
        String entityType,
        String entityName,
        String version,
        List<AssetLayer> layers,
        ColorPalette colorPalette,
        List<AnimationSpec> animations,
        NamingSpec naming,
        AssetConstraints constraints,
        Optional<AssetId> inheritsFrom,
        List<LayerBinding> bindings
) {
    public AssetSpec {
        if (entityType == null || entityType.isBlank())
            throw new IllegalArgumentException("entityType must not be blank");
        if (entityName == null || entityName.isBlank())
            throw new IllegalArgumentException("entityName must not be blank");
        if (layers == null || layers.isEmpty())
            throw new IllegalArgumentException("layers must not be empty");
        layers       = Collections.unmodifiableList(layers);
        animations   = animations   != null ? Collections.unmodifiableList(animations)   : List.of();
        bindings     = bindings     != null ? Collections.unmodifiableList(bindings)     : List.of();
        inheritsFrom = inheritsFrom != null ? inheritsFrom : Optional.empty();
        if (colorPalette == null) colorPalette = ColorPalette.projectDefault();
        if (constraints  == null) constraints  = AssetConstraints.defaults();
        if (naming       == null) naming        = new NamingSpec(entityType, entityName, null);
    }

    /**
     * Backward-compatible constructor — no inheritsFrom, no bindings.
     * All existing call-sites continue to compile without changes.
     */
    public AssetSpec(
            String entityType,
            String entityName,
            String version,
            List<AssetLayer> layers,
            ColorPalette colorPalette,
            List<AnimationSpec> animations,
            NamingSpec naming,
            AssetConstraints constraints
    ) {
        this(entityType, entityName, version, layers, colorPalette, animations,
                naming, constraints, Optional.empty(), List.of());
    }
}
