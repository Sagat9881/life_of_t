package ru.lifegame.backend.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Информация об ошибке")
public record ErrorResponseDto(
    @Schema(description = "Код ошибки", example = "SESSION_NOT_FOUND")
    String code,

    @Schema(description = "Сообщение об ошибке", example = "Session not found for user: demo-user")
    String message,

    @Schema(description = "Время возникновения ошибки", example = "2026-03-03T15:00:00Z")
    String timestamp,

    @Schema(description = "Путь запроса", example = "/api/v1/game/state")
    String path
) {}
