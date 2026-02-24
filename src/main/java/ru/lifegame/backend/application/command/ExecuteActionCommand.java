package ru.lifegame.backend.application.command;

import ru.lifegame.backend.domain.action.ActionType;

public record ExecuteActionCommand(String telegramUserId, ActionType actionType) {
    public ExecuteActionCommand {
        if (telegramUserId == null || telegramUserId.isBlank()) {
            throw new IllegalArgumentException("telegramUserId must not be blank");
        }
        if (actionType == null) {
            throw new IllegalArgumentException("actionType must not be null");
        }
    }
}
