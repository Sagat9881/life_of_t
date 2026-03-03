package ru.lifegame.backend.application.command;

public record EndDayCommand(String telegramUserId) {
    public EndDayCommand {
        if (telegramUserId == null || telegramUserId.isBlank())
            throw new IllegalArgumentException("telegramUserId must not be blank");
    }
}
