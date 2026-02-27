package ru.lifegame.backend.domain.exception;

public class InvalidActionException extends RuntimeException {
    public InvalidActionException(String message) { super(message); }
}
