package ru.lifegame.backend.infrastructure.web.exception;

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

    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleSessionNotFound(SessionNotFoundException ex,
                                                                   HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidActionException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidAction(InvalidActionException ex,
                                                                 HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage(), request);
    }

    @ExceptionHandler(NotEnoughTimeException.class)
    public ResponseEntity<ErrorResponseDto> handleNotEnoughTime(NotEnoughTimeException ex,
                                                                 HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "NOT_ENOUGH_TIME", ex.getMessage(), request);
    }

    @ExceptionHandler(ActionNotAvailableException.class)
    public ResponseEntity<ErrorResponseDto> handleActionNotAvailable(ActionNotAvailableException ex,
                                                                      HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "ACTION_NOT_AVAILABLE", ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex,
                                                                    HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneric(Exception ex, HttpServletRequest request) {
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
