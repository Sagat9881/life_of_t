package ru.lifegame.assets.domain.model.asset;

/**
 * A condition override for a layer, parsed from {@code <condition>} elements
 * within a layer's {@code <conditions>} block in visual-specs.xml.
 * <p>
 * Used for overlay layers like ambient_light where each condition
 * has different tint and opacity.
 *
 * @param id       condition identifier (e.g. "time_morning")
 * @param tint     CSS color for the overlay (e.g. "#E8F4FF")
 * @param opacity  opacity value as string (e.g. "0.12")
 * @param layerRef reference to the layer this condition applies to
 */
public record LayerCondition(
        String id,
        String tint,
        String opacity,
        String layerRef
) {
    public LayerCondition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Condition id must not be blank");
        }
    }

    public double opacityAsDouble() {
        try {
            return Double.parseDouble(opacity);
        } catch (NumberFormatException e) {
            return 0.1;
        }
    }
}
