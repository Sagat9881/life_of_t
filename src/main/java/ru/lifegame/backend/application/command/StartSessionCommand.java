package ru.lifegame.backend.application.command;

public record StartSessionCommand(String telegramUserId) {
    public StartSessionCommand {
        if (telegramUserId == null || telegramUserId.isBlank()) {
            throw new IllegalArgumentException("telegramUserId must not be blank");
        }
    }
}
