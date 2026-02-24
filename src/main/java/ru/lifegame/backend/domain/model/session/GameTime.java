package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.balance.GameBalance;

import java.util.Objects;

/**
 * Value object representing game time.
 * Immutable - all operations return new instances.
 */
public record GameTime(int day, int hour) {

    public GameTime {
        if (day < 0) {
            throw new IllegalArgumentException("Day cannot be negative: " + day);
        }
        if (hour < 0 || hour >= GameBalance.HOURS_PER_DAY) {
            throw new IllegalArgumentException("Hour must be between 0 and " + (GameBalance.HOURS_PER_DAY - 1) + ", got: " + hour);
        }
    }

    public boolean hasEnoughTime(int hours) {
        return hour + hours <= GameBalance.HOURS_PER_DAY;
    }

    public GameTime advanceHours(int hours) {
        if (hours < 0) {
            throw new IllegalArgumentException("Cannot advance by negative hours: " + hours);
        }
        int newHour = hour + hours;
        if (newHour > GameBalance.HOURS_PER_DAY) {
            throw new IllegalStateException("Advancing " + hours + " hours would exceed day limit");
        }
        return new GameTime(day, newHour);
    }

    public boolean isDayOver() {
        return hour >= GameBalance.HOURS_PER_DAY;
    }

    public GameTime startNewDay() {
        return new GameTime(day + 1, 0);
    }

    public static GameTime initial() {
        return new GameTime(0, 0);
    }
}
