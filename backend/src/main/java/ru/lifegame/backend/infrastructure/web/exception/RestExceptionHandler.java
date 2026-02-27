package ru.lifegame.backend.infrastructure.web.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import ru.lifegame.backend.domain.exception.*;
import ru.lifegame.backend.infrastructure.web.dto.ErrorResponseDto;

import java.time.Instant;

@ControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleSessionNotFound(SessionNotFoundException ex,
                                                                   HttpServletRequest request) {
        log.warn("Session not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidActionException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidAction(InvalidActionException ex,
                                                                 HttpServletRequest request) {
        log.warn("Invalid action: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage(), request);
    }

    @ExceptionHandler(NotEnoughTimeException.class)
    public ResponseEntity<ErrorResponseDto> handleNotEnoughTime(NotEnoughTimeException ex,
                                                                 HttpServletRequest request) {
        log.warn("Not enough time: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "NOT_ENOUGH_TIME", ex.getMessage(), request);
    }

    @ExceptionHandler(ActionNotAvailableException.class)
    public ResponseEntity<ErrorResponseDto> handleActionNotAvailable(ActionNotAvailableException ex,
                                                                      HttpServletRequest request) {
        log.warn("Action not available: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "ACTION_NOT_AVAILABLE", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex,
                                                                    HttpServletRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error occurred", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Internal server error", request);
    }

    private ResponseEntity<ErrorResponseDto> buildResponse(HttpStatus status, String code,
                                                            String message, HttpServletRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(code, message,
                Instant.now().toString(), request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }
}
