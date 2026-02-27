package ru.lifegame.backend.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO для представления ошибки в REST API.
 */
@Schema(description = "Информация об ошибке")
public record ErrorResponseDto(
    @Schema(description = "Код ошибки", example = "SESSION_NOT_FOUND")
    String code,
    
    @Schema(description = "Сообщение об ошибке", example = "Session not found for user: demo-user")
    String message,
    
    @Schema(description = "Время возникновения ошибки", example = "2026-02-27T15:00:00Z")
    String timestamp,
    
    @Schema(description = "Путь запроса", example = "/api/v1/game/state")
    String path
) {
}
