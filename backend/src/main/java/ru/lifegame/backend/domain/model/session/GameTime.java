package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.balance.GameBalance;

/**
 * Value object representing game time.
 * Immutable - all operations return new instances.
 * hour can be 0..24, where 24 means "end of day" (sentinel value).
 */
public record GameTime(int day, int hour) {

    public GameTime {
        if (day < 0) {
            throw new IllegalArgumentException("Day cannot be negative: " + day);
        }
        // Allow hour == HOURS_PER_DAY (24) as end-of-day sentinel
        if (hour < 0 || hour > GameBalance.HOURS_PER_DAY) {
            throw new IllegalArgumentException(
                    "Hour must be between 0 and " + GameBalance.HOURS_PER_DAY + ", got: " + hour);
        }
    }

    public boolean hasEnoughTime(int hours) {
        int remainingHours = GameBalance.HOURS_PER_DAY - hour;
        return hours <= remainingHours;
    }

    public GameTime advanceHours(int hours) {
        if (hours < 0) {
            throw new IllegalArgumentException("Cannot advance by negative hours: " + hours);
        }
        int newHour = hour + hours;
        // Clamp to end-of-day instead of throwing
        if (newHour > GameBalance.HOURS_PER_DAY) {
            newHour = GameBalance.HOURS_PER_DAY;
        }
        return new GameTime(day, newHour);
    }

    public boolean isDayOver() {
        return hour >= GameBalance.DAY_END_HOUR;
    }

    public GameTime startNewDay() {
        return new GameTime(day + 1, GameBalance.DAY_START_HOUR);
    }

    public static GameTime initial() {
        return new GameTime(1, GameBalance.DAY_START_HOUR);
    }
}
