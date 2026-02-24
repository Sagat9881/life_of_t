package ru.lifegame.backend.application.command;

public record ChooseEventOptionCommand(String telegramUserId, String eventId, String optionCode) {
    public ChooseEventOptionCommand {
        if (telegramUserId == null || telegramUserId.isBlank()) {
            throw new IllegalArgumentException("telegramUserId must not be blank");
        }
    }
}
