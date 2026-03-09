package ru.lifegame.assets.domain.model.asset;

import java.util.Collections;
import java.util.List;

/**
 * Complete specification for a layered asset, parsed from a unified XML file.
 * This is the central domain model consumed by the LayeredAssetGenerator.
 *
 * @param entityType   "characters", "locations", "pets"
 * @param entityName   snake_case name of the entity
 * @param version      spec version string
 * @param layers       ordered list of layers to generate
 * @param colorPalette color palette to use
 * @param animations   list of animation specifications (may be empty for static-only assets)
 * @param timeOfDayVariations time-of-day variations (for locations; may be empty)
 * @param naming       naming conventions
 * @param constraints  technical constraints
 */
public record AssetSpec(
        String entityType,
        String entityName,
        String version,
        List<AssetLayer> layers,
        ColorPalette colorPalette,
        List<AnimationSpec> animations,
        List<TimeOfDayVariation> timeOfDayVariations,
        NamingSpec naming,
        AssetConstraints constraints
) {
    public AssetSpec {
        if (entityType == null || entityType.isBlank()) {
            throw new IllegalArgumentException("entityType must not be blank");
        }
        if (entityName == null || entityName.isBlank()) {
            throw new IllegalArgumentException("entityName must not be blank");
        }
        if (layers == null || layers.isEmpty()) {
            throw new IllegalArgumentException("layers must not be empty");
        }
        layers = Collections.unmodifiableList(layers);
        animations = animations != null ? Collections.unmodifiableList(animations) : List.of();
        timeOfDayVariations = timeOfDayVariations != null
                ? Collections.unmodifiableList(timeOfDayVariations) : List.of();
        if (colorPalette == null) {
            colorPalette = ColorPalette.projectDefault();
        }
        if (constraints == null) {
            constraints = AssetConstraints.defaults();
        }
        if (naming == null) {
            naming = new NamingSpec(entityType, entityName, null);
        }
    }
}
