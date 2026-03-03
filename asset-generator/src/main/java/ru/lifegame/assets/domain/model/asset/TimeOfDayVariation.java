package ru.lifegame.assets.domain.model.asset;

/**
 * Colour-temperature shift applied to an asset for a specific time-of-day.
 *
 * @param timeOfDay   Label such as "morning", "day", "evening", "night".
 * @param colorShift  CSS-style hex colour used as a tint overlay.
 * @param opacity     Tint opacity in the range [0.0, 1.0].
 */
public record TimeOfDayVariation(
        String timeOfDay,
        String colorShift,
        double opacity
) {
    public TimeOfDayVariation {
        if (timeOfDay == null || timeOfDay.isBlank())
            throw new IllegalArgumentException("timeOfDay must not be blank");
        if (opacity < 0.0 || opacity > 1.0)
            throw new IllegalArgumentException("opacity must be in [0, 1]");
    }
}
