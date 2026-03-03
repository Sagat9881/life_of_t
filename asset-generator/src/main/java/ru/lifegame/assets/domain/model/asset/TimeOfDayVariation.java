package ru.lifegame.assets.domain.model.asset;

/**
 * Describes a time-of-day variation for location assets.
 *
 * @param time     time period identifier: "morning", "day", "evening", "night"
 * @param lighting lighting description
 * @param mood     mood description
 */
public record TimeOfDayVariation(
        String time,
        String lighting,
        String mood
) {
    public TimeOfDayVariation {
        if (time == null || time.isBlank()) {
            throw new IllegalArgumentException("time must not be blank");
        }
    }
}
