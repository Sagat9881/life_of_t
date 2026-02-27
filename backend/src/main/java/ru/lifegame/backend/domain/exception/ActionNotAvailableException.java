package ru.lifegame.backend.domain.exception;

/**
 * Исключение, когда действие недоступно для выполнения.
 */
public class ActionNotAvailableException extends RuntimeException {
    public ActionNotAvailableException(String message) {
        super(message);
    }
}
