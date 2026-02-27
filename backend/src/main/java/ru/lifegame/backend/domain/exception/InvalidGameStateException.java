package ru.lifegame.backend.domain.exception;

public class InvalidGameStateException extends RuntimeException {
    public InvalidGameStateException(String message) { super(message); }
}
