package ru.lifegame.backend.application.query;

public record GetStateQuery(String telegramUserId) {
    public GetStateQuery {
        if (telegramUserId == null || telegramUserId.isBlank())
            throw new IllegalArgumentException("telegramUserId must not be blank");
    }
}