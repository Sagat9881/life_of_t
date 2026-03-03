package ru.lifegame.assets.domain.model.asset;

import java.util.List;

/**
 * Top-level specification for a single game asset (character, location, pet…).
 *
 * <p>An {@code AssetSpec} is the central value object passed through every
 * layer of the asset-generation pipeline.  It is intentionally immutable;
 * all mutable state lives in the infrastructure layer.</p>
 *
 * @param id          Unique asset identifier (e.g., "tanya_idle").
 * @param category    High-level category: "character", "location", "pet", etc.
 * @param description Human-readable description used in generation prompts.
 * @param layers      Ordered list of visual layers.
 * @param animations  Animation sequences for this asset.
 * @param palettes    Named colour palettes referenced by layers.
 * @param variations  Time-of-day colour variations.
 * @param naming      File-naming conventions for output artefacts.
 * @param constraints Physical size and format constraints.
 */
public record AssetSpec(
        String id,
        String category,
        String description,
        List<AssetLayer> layers,
        List<AnimationSpec> animations,
        List<ColorPalette> palettes,
        List<TimeOfDayVariation> variations,
        NamingSpec naming,
        AssetConstraints constraints
) {
    public AssetSpec {
        if (id == null || id.isBlank())       throw new IllegalArgumentException("Asset id must not be blank");
        if (category == null || category.isBlank()) throw new IllegalArgumentException("Asset category must not be blank");
        layers     = List.copyOf(layers);
        animations = List.copyOf(animations);
        palettes   = List.copyOf(palettes);
        variations = List.copyOf(variations);
    }
}
