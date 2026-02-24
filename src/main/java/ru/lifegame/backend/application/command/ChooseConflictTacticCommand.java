package ru.lifegame.backend.application.command;

public record ChooseConflictTacticCommand(String telegramUserId, String conflictId, String tacticCode) {
    public ChooseConflictTacticCommand {
        if (telegramUserId == null || telegramUserId.isBlank()) {
            throw new IllegalArgumentException("telegramUserId must not be blank");
        }
        if (conflictId == null || conflictId.isBlank()) {
            throw new IllegalArgumentException("conflictId must not be blank");
        }
        if (tacticCode == null || tacticCode.isBlank()) {
            throw new IllegalArgumentException("tacticCode must not be blank");
        }
    }
}
