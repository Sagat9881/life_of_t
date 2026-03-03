package ru.lifegame.backend.domain.exception;

public class ActionNotAvailableException extends RuntimeException {
    public ActionNotAvailableException(String message) { super(message); }
}
