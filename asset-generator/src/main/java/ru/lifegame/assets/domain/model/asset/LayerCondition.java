package ru.lifegame.assets.domain.model.asset;

/**
 * A condition override for a layer, parsed from {@code <condition>} elements
 * within a layer's {@code <conditions>} block in visual-specs.xml.
 * <p>
 * Used for overlay layers like ambient_light where each time-of-day
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

    /**
     * Extracts time-of-day value from the condition id.
     * E.g. "time_morning" → "morning", "time_night" → "night".
     * Returns the full id if no "time_" prefix.
     */
    public String timeOfDayValue() {
        if (id.startsWith("time_")) {
            return id.substring(5);
        }
        return id;
    }

    public double opacityAsDouble() {
        try {
            return Double.parseDouble(opacity);
        } catch (NumberFormatException e) {
            return 0.1;
        }
    }
}
