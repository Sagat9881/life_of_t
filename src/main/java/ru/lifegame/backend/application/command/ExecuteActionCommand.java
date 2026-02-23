package ru.lifegame.backend.application.command;

public record ExecuteActionCommand(String telegramUserId, String actionCode) {
    public ExecuteActionCommand {
        if (telegramUserId == null || telegramUserId.isBlank())
            throw new IllegalArgumentException("telegramUserId must not be blank");
        if (actionCode == null || actionCode.isBlank())
            throw new IllegalArgumentException("actionCode must not be blank");
    }
}