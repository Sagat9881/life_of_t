package ru.lifegame.backend.domain.exception;

public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException(String telegramUserId) {
        super("Session not found for user: " + telegramUserId);
    }
}
