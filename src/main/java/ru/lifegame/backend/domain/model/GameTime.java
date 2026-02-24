package ru.lifegame.backend.domain.model;

import ru.lifegame.backend.domain.balance.GameBalance;

public record GameTime(int day, int hour) {

    public GameTime advanceHours(int hours) {
        int newHour = hour + hours;
        if (newHour >= GameBalance.DAY_END_HOUR) {
            return new GameTime(day, GameBalance.DAY_END_HOUR);
        }
        return new GameTime(day, newHour);
    }

    public GameTime startNewDay() {
        return new GameTime(day + 1, GameBalance.DAY_START_HOUR);
    }

    public boolean hasEnoughTime(int hoursNeeded) {
        return hour + hoursNeeded <= GameBalance.DAY_END_HOUR;
    }

    public boolean isDayOver() {
        return hour >= GameBalance.DAY_END_HOUR;
    }

    public static GameTime initial() {
        return new GameTime(1, GameBalance.DAY_START_HOUR);
    }
}
