package ru.lifegame.backend.domain.exception;

public class NotEnoughTimeException extends RuntimeException {
    public NotEnoughTimeException(String message) { super(message); }
}
